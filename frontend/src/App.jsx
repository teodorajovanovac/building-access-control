import { Navigate, Route, Routes } from 'react-router-dom';
import { useAuth } from './context/AuthContext';
import ProtectedRoute from './routing/ProtectedRoute';
import AppLayout from './components/layout/AppLayout';

import LoginPage from './pages/auth/LoginPage';
import RegisterPage from './pages/auth/RegisterPage';
import PublicGatePassPage from './pages/public/PublicGatePassPage';
import ForbiddenPage from './pages/misc/ForbiddenPage';
import NotFoundPage from './pages/misc/NotFoundPage';

import ResidentProfilePage from './pages/resident/ResidentProfilePage';
import MyGatePassesPage from './pages/resident/MyGatePassesPage';
import GatePassFormPage from './pages/resident/GatePassFormPage';
import GatePassDetailPage from './pages/resident/GatePassDetailPage';
import ResidentEntryLogsPage from './pages/resident/ResidentEntryLogsPage';

import ScanPage from './pages/security/ScanPage';
import ManualSearchPage from './pages/security/ManualSearchPage';

import BuildingsPage from './pages/admin/BuildingsPage';
import ApartmentsPage from './pages/admin/ApartmentsPage';
import UsersPage from './pages/admin/UsersPage';
import StaffPage from './pages/admin/StaffPage';
import GatePassesSearchPage from './pages/admin/GatePassesSearchPage';

import EntryLogsPage from './pages/shared/EntryLogsPage';
import DenialsPage from './pages/shared/DenialsPage';
import StatisticsPage from './pages/shared/StatisticsPage';

const HOME_BY_ROLE = {
  RESIDENT: '/resident/profile',
  SECURITY: '/security/scan',
  ADMIN: '/admin/buildings',
};

function HomeRedirect() {
  const { isAuthenticated, user } = useAuth();
  if (!isAuthenticated) return <Navigate to="/login" replace />;
  return <Navigate to={HOME_BY_ROLE[user.role] || '/login'} replace />;
}

export default function App() {
  return (
    <Routes>
      {/* Javne rute */}
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route path="/pass/:code" element={<PublicGatePassPage />} />
      <Route path="/403" element={<ForbiddenPage />} />

      <Route path="/" element={<HomeRedirect />} />

      {/* Stanar */}
      <Route element={<ProtectedRoute roles={['RESIDENT']} />}>
        <Route element={<AppLayout />}>
          <Route path="/resident/profile" element={<ResidentProfilePage />} />
          <Route path="/resident/gatepasses" element={<MyGatePassesPage />} />
          <Route path="/resident/gatepasses/new" element={<GatePassFormPage />} />
          <Route path="/resident/gatepasses/:id" element={<GatePassDetailPage />} />
          <Route path="/resident/gatepasses/:id/edit" element={<GatePassFormPage />} />
          <Route path="/resident/entrylogs" element={<ResidentEntryLogsPage />} />
        </Route>
      </Route>

      {/* Obezbeđenje */}
      <Route element={<ProtectedRoute roles={['SECURITY']} />}>
        <Route element={<AppLayout />}>
          <Route path="/security/scan" element={<ScanPage />} />
          <Route path="/security/search" element={<ManualSearchPage />} />
          <Route path="/security/logs" element={<EntryLogsPage />} />
          <Route path="/security/denials" element={<DenialsPage />} />
          <Route path="/security/statistics" element={<StatisticsPage />} />
        </Route>
      </Route>

      {/* Administrator */}
      <Route element={<ProtectedRoute roles={['ADMIN']} />}>
        <Route element={<AppLayout />}>
          <Route path="/admin/buildings" element={<BuildingsPage />} />
          <Route path="/admin/apartments" element={<ApartmentsPage />} />
          <Route path="/admin/users" element={<UsersPage />} />
          <Route path="/admin/staff" element={<StaffPage />} />
          <Route path="/admin/gatepasses" element={<GatePassesSearchPage />} />
          <Route path="/admin/entrylogs" element={<EntryLogsPage />} />
          <Route path="/admin/denials" element={<DenialsPage />} />
          <Route path="/admin/statistics" element={<StatisticsPage />} />
        </Route>
      </Route>

      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  );
}
