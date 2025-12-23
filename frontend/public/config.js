// Configuration - Automatically detects environment
const CONFIG = {
    // Detects environment and uses appropriate URLs
    API_URL: (() => {
        if (window.location.hostname === 'localhost') {
            return 'http://localhost:8080/api';
        }
        // Use environment variable from env.js
        const backendUrl = window.ENV_BACKEND_URL || 'https://whatsapp-backend-paww.onrender.com';
        return backendUrl + '/api';
    })(),
    
    WS_URL: (() => {
        if (window.location.hostname === 'localhost') {
            return 'ws://localhost:8081';
        }
        // Use environment variable from env.js
        const backendUrl = window.ENV_BACKEND_URL || 'https://whatsapp-backend-paww.onrender.com';
        return backendUrl.replace('https://', 'wss://');
    })()
};

// Export API_URL globally for script.js
const API_URL = CONFIG.API_URL;
const WS_URL = CONFIG.WS_URL;

console.log('✓ Config loaded');
console.log('Backend URL:', window.ENV_BACKEND_URL);
console.log('API URL:', API_URL);
console.log('WebSocket URL:', WS_URL);
