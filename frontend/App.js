import React from 'react';
import { SafeAreaView, View, StyleSheet } from 'react-native';
import { StatusBar } from 'expo-status-bar';
import { PrimaryButton, Card } from '@crm/ui-kit';
import { AuthProvider, useAuth } from '@crm/auth-module';

const Dashboard = () => {
  const { user, login } = useAuth();

  return (
    <View style={styles.content}>
      <Card title="Micro Frontend Status">
        <PrimaryButton 
          label={user ? `Welcome, ${user.name}` : "Login to CRM"} 
          onPress={() => login('TechLead', 'password')} 
        />
      </Card>
    </View>
  );
};

export default function App() {
  return (
    <AuthProvider>
      <SafeAreaView style={styles.container}>
        <Dashboard />
        <StatusBar style="auto" />
      </SafeAreaView>
    </AuthProvider>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f5f5f5',
  },
  content: {
    flex: 1,
    justifyContent: 'center',
    padding: 20,
  }
});
