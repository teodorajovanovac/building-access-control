import { useEffect, useState } from 'react';
import { Link as RouterLink, useNavigate } from 'react-router-dom';
import {
  Box,
  Paper,
  TextField,
  MenuItem,
  Button,
  Typography,
  Stack,
  Link,
  CircularProgress,
  Alert,
} from '@mui/material';
import HowToRegIcon from '@mui/icons-material/HowToReg';
import { register } from '../../api/auth';
import { getPublicBuildings } from '../../api/buildings';
import { getPublicApartments } from '../../api/apartments';
import { extractErrorMessage } from '../../api/client';
import ErrorAlert from '../../components/common/ErrorAlert';

export default function RegisterPage() {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    firstName: '',
    lastName: '',
    email: '',
    password: '',
    apartmentId: '',
  });
  const [buildingId, setBuildingId] = useState('');
  const [buildings, setBuildings] = useState([]);
  const [apartments, setApartments] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);

  const update = (field) => (e) => setForm((f) => ({ ...f, [field]: e.target.value }));

  useEffect(() => {
    getPublicBuildings()
      .then(setBuildings)
      .catch((err) => setError(extractErrorMessage(err)));
  }, []);

  useEffect(() => {
    setForm((f) => ({ ...f, apartmentId: '' }));
    if (!buildingId) {
      setApartments([]);
      return;
    }
    getPublicApartments(buildingId)
      .then(setApartments)
      .catch((err) => setError(extractErrorMessage(err)));
  }, [buildingId]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      await register({
        ...form,
        apartmentId: Number(form.apartmentId),
      });
      setSuccess(true);
      setTimeout(() => navigate('/login'), 1800);
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  return (
    <Box
      sx={{
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        minHeight: "100vh",
        width: "100%",
        bgcolor: 'background.default',
        p: 2
      }}>
      <Paper elevation={2} sx={{ p: 4, maxWidth: 460, width: '100%' }}>
        <Stack
          spacing={1}
          sx={{
            alignItems: "center",
            mb: 3
          }}>
          <HowToRegIcon color="primary" sx={{ fontSize: 40 }} />
          <Typography variant="h5" sx={{
            fontWeight: 700
          }}>
            Registracija stanara
          </Typography>
          <Typography
            variant="body2"
            sx={{
              color: "text.secondary",
              textAlign: "center"
            }}>
            Nakon registracije automatski dobijate lični bedž kod i QR kod za ulazak.
          </Typography>
        </Stack>

        <ErrorAlert message={error} />
        {success && (
          <Alert severity="success" sx={{ mb: 2 }}>
            Nalog je uspešno kreiran! Preusmeravamo vas na prijavu…
          </Alert>
        )}

        <Box component="form" onSubmit={handleSubmit} noValidate>
          <Stack spacing={2}>
            <Stack direction="row" spacing={2}>
              <TextField label="Ime" value={form.firstName} onChange={update('firstName')} required fullWidth />
              <TextField label="Prezime" value={form.lastName} onChange={update('lastName')} required fullWidth />
            </Stack>
            <TextField
              label="Email"
              type="email"
              value={form.email}
              onChange={update('email')}
              required
              fullWidth
            />
            <TextField
              label="Lozinka"
              type="password"
              value={form.password}
              onChange={update('password')}
              helperText="Najmanje 6 karaktera"
              required
              fullWidth
            />
            <TextField
              select
              label="Zgrada"
              value={buildingId}
              onChange={(e) => setBuildingId(e.target.value)}
              required
              fullWidth
            >
              {buildings.map((b) => (
                <MenuItem key={b.id} value={String(b.id)}>
                  {b.name} — {b.address}
                </MenuItem>
              ))}
            </TextField>
            <TextField
              select
              label="Stan"
              value={form.apartmentId}
              onChange={update('apartmentId')}
              required
              fullWidth
              disabled={!buildingId}
              helperText={!buildingId ? 'Prvo izaberite zgradu' : undefined}
            >
              {apartments.map((a) => (
                <MenuItem key={a.id} value={String(a.id)}>
                  Stan {a.number} (sprat {a.floor})
                </MenuItem>
              ))}
            </TextField>
            <Button
              type="submit"
              variant="contained"
              size="large"
              disabled={loading}
              startIcon={loading ? <CircularProgress size={18} color="inherit" /> : null}
            >
              Registruj se
            </Button>
          </Stack>
        </Box>

        <Typography variant="body2" sx={{ mt: 3, textAlign: 'center' }}>
          Već imate nalog?{' '}
          <Link component={RouterLink} to="/login">
            Prijavite se
          </Link>
        </Typography>
      </Paper>
    </Box>
  );
}
