import React from 'react';
import { TouchableOpacity, Text, View, StyleSheet } from 'react-native';

export const PrimaryButton = ({ label, onPress }) => (
  <TouchableOpacity style={styles.btn} onPress={onPress}>
    <Text style={styles.text}>{label}</Text>
  </TouchableOpacity>
);

export const Card = ({ title, children }) => (
  <View style={styles.card}>
    <Text style={styles.title}>{title}</Text>
    {children}
  </View>
);

const styles = StyleSheet.create({
  btn: { backgroundColor: '#2563eb', padding: 15, borderRadius: 8, alignItems: 'center' },
  text: { color: 'white', fontWeight: 'bold' },
  card: { backgroundColor: 'white', padding: 20, borderRadius: 12, shadowOpacity: 0.1, elevation: 3 },
  title: { fontSize: 18, marginBottom: 15, fontWeight: '600' }
});
