import { useState } from 'react';
import { Link as RouterLink, useLocation, useNavigate } from 'react-router-dom';
import {
  Box,
  Paper,
  TextField,
  Button,
  Typography,
  Stack,
  Link,
  CircularProgress,
} from '@mui/material';
import SecurityIcon from '@mui/icons-material/Security';
import { useAuth } from '../../context/AuthContext';
import { extractErrorMessage } from '../../api/client';
import ErrorAlert from '../../components/common/ErrorAlert';

const HOME_BY_ROLE = {
  RESIDENT: '/resident/profile',
  SECURITY: '/security/scan',
  ADMIN: '/admin/buildings',
};

export default function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const user = await login(email, password);
      const from = location.state?.from?.pathname;
      navigate(from || HOME_BY_ROLE[user.role] || '/', { replace: true });
    } catch (err) {
      setError(extractErrorMessage(err) || 'Pogrešan email ili lozinka!');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Box
      display="flex"
      alignItems="center"
      justifyContent="center"
      minHeight="100vh"
      sx={{ bgcolor: 'background.default', p: 2 }}
    >
      <Paper elevation={2} sx={{ p: 4, maxWidth: 420, width: '100%' }}>
        <Stack alignItems="center" spacing={1} sx={{ mb: 3 }}>
          <SecurityIcon color="primary" sx={{ fontSize: 40 }} />
          <Typography variant="h5" fontWeight={700}>
            Kontrola pristupa zgradi
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Prijavite se na svoj nalog
          </Typography>
        </Stack>

        <ErrorAlert message={error} />

        <Box component="form" onSubmit={handleSubmit} noValidate>
          <Stack spacing={2}>
            <TextField
              label="Email"
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
              fullWidth
              autoFocus
            />
            <TextField
              label="Lozinka"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              fullWidth
            />
            <Button
              type="submit"
              variant="contained"
              size="large"
              disabled={loading}
              startIcon={loading ? <CircularProgress size={18} color="inherit" /> : null}
            >
              Prijavi se
            </Button>
          </Stack>
        </Box>

        <Typography variant="body2" sx={{ mt: 3, textAlign: 'center' }}>
          Nemate nalog?{' '}
          <Link component={RouterLink} to="/register">
            Registrujte se kao stanar
          </Link>
        </Typography>
      </Paper>
    </Box>
  );
}
