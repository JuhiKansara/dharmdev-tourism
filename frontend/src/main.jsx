import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import './index.css';
import CreateEstimatePage from './pages/CreateEstimatePage.jsx';
import EstimatePreviewPage from './pages/EstimatePreviewPage.jsx';

createRoot(document.getElementById('root')).render(
    <StrictMode>
        <BrowserRouter>
            <Routes>
                <Route path="/" element={<CreateEstimatePage />} />
                <Route path="/estimates/:id" element={<EstimatePreviewPage />} />
            </Routes>
        </BrowserRouter>
    </StrictMode>,
);