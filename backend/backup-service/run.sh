#!/bin/bash

# Configuration
BACKUP_DIR="/tmp/backups"
RCLONE_REMOTE_NAME="gdrive_backup"
RETENTION_DAYS=7

mkdir -p $BACKUP_DIR

echo "[INFO] Starting Backup Service..."
echo "[INFO] Database Host: $POSTGRES_HOST"
echo "[INFO] Google Drive Folder ID: $GOOGLE_FOLDER_ID"

export RCLONE_CONFIG_GDRIVE_BACKUP_TYPE=drive
export RCLONE_CONFIG_GDRIVE_BACKUP_SCOPE=drive
export RCLONE_CONFIG_GDRIVE_BACKUP_SERVICE_ACCOUNT_FILE=$GOOGLE_SERVICE_ACCOUNT_FILE
export RCLONE_CONFIG_GDRIVE_BACKUP_TEAM_DRIVE="" 

while true; do
    TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
    FILENAME="backup_${POSTGRES_DB}_${TIMESTAMP}.sql.gz"
    FILEPATH="${BACKUP_DIR}/${FILENAME}"

    echo "----------------------------------------------------------------"
    echo "[START] Backup started at $(date)"

    # 1. Dump Database (Compressed)
    # PGPASSWORD is implicitly used by pg_dump from env var
    if PGPASSWORD=$POSTGRES_PASSWORD pg_dump -h $POSTGRES_HOST -U $POSTGRES_USER -d $POSTGRES_DB -Fc > "$FILEPATH"; then
        echo "[SUCCESS] Database dumped locally: $FILENAME"

        # 2. Upload to Google Drive (root_folder_id is supported via --drive-root-folder-id)
        echo "[UPLOAD] Uploading to Google Drive..."
        if rclone copy "$FILEPATH" "$RCLONE_REMOTE_NAME:" --drive-root-folder-id "$GOOGLE_FOLDER_ID"; then
            echo "[SUCCESS] Uploaded to Google Drive."
            
            # 3. Cleanup Local File (Stateless Container)
            rm "$FILEPATH"
        else
            echo "[ERROR] Failed to upload to Google Drive."
        fi

        # 4. Cleanup Remote Files (Retention Policy)
        # Delete files older than RETENTION_DAYS on Google Drive
        echo "[CLEANUP] Checking for old backups on Drive (> $RETENTION_DAYS days)..."
        rclone delete "$RCLONE_REMOTE_NAME:" --drive-root-folder-id "$GOOGLE_FOLDER_ID" --min-age "${RETENTION_DAYS}d"
        
    else
        echo "[ERROR] pg_dump failed!"
    fi

    echo "[DONE] Process finished. Sleeping for 24 hours..."
    echo "----------------------------------------------------------------"
    
    # Sleep for 24 hours (86400 seconds)
    sleep 86400
done