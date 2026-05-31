import React from 'react';
import { createRoot } from 'react-dom/client';
import App from './App';

// Find the root element in index.html
const container = document.getElementById('root');

if (!container) {
  throw new Error("Failed to find the root element. Make sure index.html has a <div id='root'></div>");
}

// Create a React root and render the App component
const root = createRoot(container);
root.render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);