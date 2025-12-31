package com.crm.notification.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // =================================================================================
    // 1. BUSINESS EXCEPTIONS (Lỗi Nghiệp vụ - UserFacing = true)
    // Những lỗi này an toàn để hiển thị thông báo chi tiết cho người dùng cuối.
    // =================================================================================

    // --- Nhóm 10xx: Validation & Input (Dữ liệu đầu vào) ---
    INVALID_REQUEST(1000, "Yêu cầu không hợp lệ.", HttpStatus.BAD_REQUEST, true),
    MISSING_REQUIRED_FIELD(1001, "Thiếu thông tin bắt buộc. Vui lòng kiểm tra lại.", HttpStatus.BAD_REQUEST, true),
    INVALID_INPUT_DATA(1002, "Dữ liệu đầu vào không đúng định dạng.", HttpStatus.BAD_REQUEST, true),
    INVALID_EMAIL_FORMAT(1003, "Định dạng email không hợp lệ.", HttpStatus.BAD_REQUEST, true),
    INVALID_PHONE_FORMAT(1004, "Số điện thoại không đúng định dạng.", HttpStatus.BAD_REQUEST, true),
    INVALID_DATE_FORMAT(1005, "Định dạng ngày tháng không hợp lệ.", HttpStatus.BAD_REQUEST, true),
    INVALID_PAGEABLE(1006, "Thông tin phân trang không hợp lệ.", HttpStatus.BAD_REQUEST, true),
    INVALID_ID_FORMAT(1007, "Định dạng ID không hợp lệ.", HttpStatus.BAD_REQUEST, true),
    REQUEST_BODY_INVALID(1008, "Nội dung yêu cầu (Body) bị thiếu hoặc sai cấu trúc.", HttpStatus.BAD_REQUEST, true),
    MAX_FILE_SIZE_EXCEEDED(1009, "Dung lượng tệp tin vượt quá giới hạn cho phép.", HttpStatus.BAD_REQUEST, true),
    INVALID_FILE_EXTENSION(1010, "Định dạng tệp tin không được hỗ trợ.", HttpStatus.BAD_REQUEST, true),

    // --- Nhóm 11xx: Authentication & User Account (Tài khoản & Xác thực - Business side) ---
    USER_EXISTED(1100, "Tài khoản hoặc email đã tồn tại trong hệ thống.", HttpStatus.BAD_REQUEST, true),
    PHONE_EXISTED(1101, "Số điện thoại này đã được đăng ký.", HttpStatus.BAD_REQUEST, true),
    ACCOUNT_LOCKED(1102, "Tài khoản của bạn đã bị khóa. Vui lòng liên hệ quản trị viên.", HttpStatus.BAD_REQUEST, true),
    ACCOUNT_NOT_VERIFIED(1103, "Tài khoản chưa được xác thực email/SĐT.", HttpStatus.BAD_REQUEST, true),
    INVALID_LOGIN_CREDENTIALS(1104, "Tên đăng nhập hoặc mật khẩu không chính xác.", HttpStatus.BAD_REQUEST, true),
    OLD_PASSWORD_INCORRECT(1105, "Mật khẩu hiện tại không đúng.", HttpStatus.BAD_REQUEST, true),
    PASSWORD_COMPLEXITY_FAILED(1106, "Mật khẩu phải chứa ít nhất 8 ký tự, bao gồm chữ hoa, chữ thường và số.", HttpStatus.BAD_REQUEST, true),
    OTP_INVALID(1107, "Mã OTP không chính xác.", HttpStatus.BAD_REQUEST, true),
    OTP_EXPIRED(1108, "Mã OTP đã hết hạn. Vui lòng yêu cầu mã mới.", HttpStatus.BAD_REQUEST, true),
    REFRESH_TOKEN_INVALID(1109, "Phiên đăng nhập không hợp lệ hoặc đã hết hạn.", HttpStatus.BAD_REQUEST, true),

    // --- Nhóm 12xx: CRM Core - Customers & Leads (Khách hàng & Tiềm năng) ---
    LEAD_ALREADY_CONVERTED(1200, "Khách hàng tiềm năng này đã được chuyển đổi thành công.", HttpStatus.BAD_REQUEST, true),
    LEAD_STATUS_INVALID(1201, "Trạng thái Lead không hợp lệ cho thao tác này.", HttpStatus.BAD_REQUEST, true),
    CUSTOMER_TYPE_MISMATCH(1202, "Loại khách hàng không phù hợp với chiến dịch này.", HttpStatus.BAD_REQUEST, true),
    DUPLICATE_CONTACT(1203, "Liên hệ này đã tồn tại trong hồ sơ khách hàng.", HttpStatus.BAD_REQUEST, true),
    CONTACT_LIMIT_EXCEEDED(1204, "Bạn đã đạt giới hạn số lượng liên hệ cho phép.", HttpStatus.BAD_REQUEST, true),
    MERGE_CONFLICT_CUSTOMER(1205, "Không thể gộp khách hàng do xung đột dữ liệu.", HttpStatus.BAD_REQUEST, true),

    // --- Nhóm 13xx: CRM Sales - Deals & Pipelines (Cơ hội & Quy trình bán hàng) ---
    DEAL_STAGE_INVALID(1300, "Giai đoạn cơ hội không hợp lệ hoặc sai quy trình.", HttpStatus.BAD_REQUEST, true),
    DEAL_CLOSED_CANNOT_EDIT(1301, "Không thể chỉnh sửa cơ hội đã Đóng/Thất bại.", HttpStatus.BAD_REQUEST, true),
    PIPELINE_CANNOT_DELETE(1302, "Không thể xóa quy trình đang chứa cơ hội hoạt động.", HttpStatus.BAD_REQUEST, true),
    INVALID_CURRENCY(1303, "Đơn vị tiền tệ không được hỗ trợ.", HttpStatus.BAD_REQUEST, true),
    DISCOUNT_EXCEEDS_LIMIT(1304, "Mức chiết khấu vượt quá giới hạn cho phép.", HttpStatus.BAD_REQUEST, true),
    QUOTE_EXPIRED(1305, "Báo giá này đã hết hiệu lực.", HttpStatus.BAD_REQUEST, true),

    // --- Nhóm 14xx: CRM Support - Tickets & Activities (Hỗ trợ & Công việc) ---
    TICKET_STATUS_INVALID(1400, "Trạng thái yêu cầu hỗ trợ không hợp lệ.", HttpStatus.BAD_REQUEST, true),
    TICKET_ALREADY_ASSIGNED(1401, "Yêu cầu này đã được gán cho nhân viên khác.", HttpStatus.BAD_REQUEST, true),
    TASK_DUE_DATE_INVALID(1402, "Ngày hết hạn công việc không thể nhỏ hơn ngày hiện tại.", HttpStatus.BAD_REQUEST, true),
    ACTIVITY_LOG_FAILED(1403, "Không thể ghi nhật ký hoạt động.", HttpStatus.BAD_REQUEST, true),
    
    // --- Nhóm 15xx: Payments & Contracts (Thanh toán & Hợp đồng) ---
    PAYMENT_FAILED(1500, "Thanh toán thất bại. Vui lòng kiểm tra thẻ hoặc số dư.", HttpStatus.BAD_REQUEST, true),
    CONTRACT_NOT_SIGNED(1501, "Hợp đồng chưa được ký kết.", HttpStatus.BAD_REQUEST, true),
    CONTRACT_EXPIRED(1502, "Hợp đồng đã hết hạn.", HttpStatus.BAD_REQUEST, true),
    INVALID_PAYMENT_METHOD(1503, "Phương thức thanh toán không hợp lệ.", HttpStatus.BAD_REQUEST, true),
    TRANSACTION_LIMIT_EXCEEDED(1504, "Giao dịch vượt quá hạn mức cho phép.", HttpStatus.BAD_REQUEST, true),

    // --- Nhóm 40xx: Resource Not Found (Không tìm thấy dữ liệu - CRM Context) ---
    RESOURCE_NOT_FOUND(4000, "Không tìm thấy tài nguyên yêu cầu.", HttpStatus.NOT_FOUND, true),
    USER_NOT_FOUND(4001, "Không tìm thấy người dùng.", HttpStatus.NOT_FOUND, true),
    ROLE_NOT_FOUND(4002, "Không tìm thấy vai trò hoặc quyền hạn.", HttpStatus.NOT_FOUND, true),
    
    // CRM Entities Not Found
    CUSTOMER_NOT_FOUND(4010, "Không tìm thấy hồ sơ khách hàng.", HttpStatus.NOT_FOUND, true),
    LEAD_NOT_FOUND(4011, "Không tìm thấy khách hàng tiềm năng.", HttpStatus.NOT_FOUND, true),
    CONTACT_NOT_FOUND(4012, "Không tìm thấy thông tin liên hệ.", HttpStatus.NOT_FOUND, true),
    DEAL_NOT_FOUND(4013, "Không tìm thấy cơ hội kinh doanh.", HttpStatus.NOT_FOUND, true),
    PIPELINE_NOT_FOUND(4014, "Không tìm thấy quy trình bán hàng.", HttpStatus.NOT_FOUND, true),
    STAGE_NOT_FOUND(4015, "Không tìm thấy giai đoạn bán hàng.", HttpStatus.NOT_FOUND, true),
    PRODUCT_NOT_FOUND(4016, "Không tìm thấy sản phẩm hoặc dịch vụ.", HttpStatus.NOT_FOUND, true),
    CONTRACT_NOT_FOUND(4017, "Không tìm thấy hợp đồng.", HttpStatus.NOT_FOUND, true),
    TICKET_NOT_FOUND(4018, "Không tìm thấy phiếu hỗ trợ.", HttpStatus.NOT_FOUND, true),
    TASK_NOT_FOUND(4019, "Không tìm thấy công việc.", HttpStatus.NOT_FOUND, true),
    NOTIFICATION_NOT_FOUND(4020, "Không tìm thấy thông báo.", HttpStatus.NOT_FOUND, true),

    // --- Nhóm 60xx: Conflicts (Xung đột dữ liệu) ---
    DATA_CONFLICT(6000, "Dữ liệu bị xung đột, vui lòng tải lại trang.", HttpStatus.CONFLICT, true),
    DUPLICATE_RECORD(6001, "Bản ghi đã tồn tại trong hệ thống.", HttpStatus.CONFLICT, true),


    // =================================================================================
    // 2. SYSTEM & SECURITY EXCEPTIONS (Lỗi Hệ thống & Bảo mật - UserFacing = false/mixed)
    // Các lỗi này thường liên quan đến Auth Token hoặc Lỗi Server nội bộ.
    // =================================================================================

    // --- Nhóm 20xx: Unauthorized (Lỗi xác thực - 401) ---
    UNAUTHENTICATED(2000, "Vui lòng đăng nhập để tiếp tục.", HttpStatus.UNAUTHORIZED, false),
    TOKEN_EXPIRED(2001, "Phiên đăng nhập đã hết hạn.", HttpStatus.UNAUTHORIZED, false),
    TOKEN_INVALID(2002, "Token xác thực không hợp lệ.", HttpStatus.UNAUTHORIZED, false),
    TOKEN_REVOKED(2003, "Token đã bị thu hồi.", HttpStatus.UNAUTHORIZED, false),
    HEADER_TOKEN_MISSING(2004, "Thiếu Header xác thực.", HttpStatus.UNAUTHORIZED, false),

    // --- Nhóm 30xx: Forbidden (Lỗi phân quyền - 403) ---
    ACCESS_DENIED(3000, "Bạn không có quyền truy cập tài nguyên này.", HttpStatus.FORBIDDEN, true),
    NOT_ADMIN(3001, "Yêu cầu quyền quản trị viên.", HttpStatus.FORBIDDEN, true),
    NOT_MANAGER(3002, "Yêu cầu quyền quản lý.", HttpStatus.FORBIDDEN, true),
    DATA_ACCESS_RESTRICTED(3003, "Bạn không có quyền xem dữ liệu của chi nhánh/nhóm này.", HttpStatus.FORBIDDEN, true),

    // --- Nhóm 50xx & 90xx: Internal System Errors (Lỗi nội bộ - 500/503) ---
    UNCATEGORIZED_EXCEPTION(9999, "Lỗi hệ thống không xác định. Vui lòng thử lại sau.", HttpStatus.INTERNAL_SERVER_ERROR, false),
    
    // Database & Storage
    DB_CONNECTION_FAILED(9001, "Không thể kết nối cơ sở dữ liệu.", HttpStatus.INTERNAL_SERVER_ERROR, false),
    DB_QUERY_FAILED(9002, "Lỗi truy vấn dữ liệu.", HttpStatus.INTERNAL_SERVER_ERROR, false),
    FILE_UPLOAD_ERROR(9003, "Lỗi hệ thống khi tải lên tệp tin.", HttpStatus.INTERNAL_SERVER_ERROR, false),
    
    // External Services
    EMAIL_SENDING_FAILED(9100, "Lỗi hệ thống gửi email.", HttpStatus.INTERNAL_SERVER_ERROR, false),
    SMS_SENDING_FAILED(9101, "Lỗi hệ thống gửi SMS.", HttpStatus.INTERNAL_SERVER_ERROR, false),
    PAYMENT_GATEWAY_ERROR(9102, "Lỗi kết nối cổng thanh toán.", HttpStatus.INTERNAL_SERVER_ERROR, false),
    AI_SERVICE_ERROR(9103, "Lỗi xử lý dịch vụ AI.", HttpStatus.INTERNAL_SERVER_ERROR, false),
    
    // Infrastructure
    REDIS_CONNECTION_FAILED(9500, "Không thể kết nối đến Cache Server (Redis).", HttpStatus.SERVICE_UNAVAILABLE, false),
    SERVICE_UNAVAILABLE(9501, "Dịch vụ tạm thời không khả dụng.", HttpStatus.SERVICE_UNAVAILABLE, false);

    private final int code;
    private final String message;
    private final HttpStatus statusCode;
    private final boolean userFacing;

    ErrorCode(int code, String message, HttpStatus statusCode, boolean userFacing) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
        this.userFacing = userFacing;
    }
}