import axios from 'axios';

const API_URL = 'http://localhost:8000/api/v1'; 

const client = axios.create({
  baseURL: API_URL,
  timeout: 10000,
});

export const get = (path) => client.get(path);
export const post = (path, data) => client.post(path, data);
export default client;
