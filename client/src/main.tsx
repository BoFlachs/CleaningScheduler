import React from 'react'
import ReactDOM from 'react-dom/client'
import './index.css'
import { RouterProvider } from 'react-router-dom'
import { router } from './router.tsx'
import { CleaningSchedulerProvider } from './contexts/CleaningSchedulerContext.tsx'

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <CleaningSchedulerProvider>
        <RouterProvider router={router}></RouterProvider>
    </CleaningSchedulerProvider>
  </React.StrictMode>,
)
