import { useState } from 'react';
import {
  Paper,
  Box,
  TextField,
  Button,
  Stack,
  Typography,
  Collapse,
  Alert,
  Divider,
  CircularProgress,
} from '@mui/material';
import QrCodeScannerIcon from '@mui/icons-material/QrCodeScanner';
import SendIcon from '@mui/icons-material/Send';
import BlockIcon from '@mui/icons-material/Block';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import LoginIcon from '@mui/icons-material/Login';
import LogoutIcon from '@mui/icons-material/Logout';
import CancelIcon from '@mui/icons-material/Cancel';
import { scanCode, denyManually } from '../../api/access';
import { extractErrorMessage } from '../../api/client';
import PageHeader from '../../components/common/PageHeader';
import QrScannerPanel from '../../components/common/QrScannerPanel';
import ErrorAlert from '../../components/common/ErrorAlert';

const OUTCOME_META = {
  GUEST_ENTRY_APPROVED: { color: 'success', icon: <CheckCircleIcon fontSize="large" /> },
  ENTRY_RECORDED: { color: 'success', icon: <LoginIcon fontSize="large" /> },
  EXIT_RECORDED: { color: 'info', icon: <LogoutIcon fontSize="large" /> },
  DENIED: { color: 'error', icon: <CancelIcon fontSize="large" /> },
};

function formatTime(value) {
  if (!value) return null;
  return new Date(value).toLocaleString('sr-RS');
}

export default function ScanPage() {
  const [code, setCode] = useState('');
  const [cameraOpen, setCameraOpen] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [result, setResult] = useState(null);

  const [denyOpen, setDenyOpen] = useState(false);
  const [denyReason, setDenyReason] = useState('');
  const [denyLoading, setDenyLoading] = useState(false);

  const submitScan = async (value) => {
    const trimmed = (value ?? code).trim();
    if (!trimmed) return;
    setError('');
    setResult(null);
    setLoading(true);
    try {
      const res = await scanCode(trimmed);
      setResult(res);
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  const handleCameraScan = (decodedText) => {
    setCode(decodedText);
    setCameraOpen(false);
    submitScan(decodedText);
  };

  const handleDeny = async () => {
    if (!denyReason.trim()) return;
    setDenyLoading(true);
    setError('');
    try {
      const res = await denyManually({ code: code.trim() || null, personName: null, reasonNote: denyReason.trim() });
      setResult(res);
      setDenyOpen(false);
      setDenyReason('');
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setDenyLoading(false);
    }
  };

  const meta = result ? OUTCOME_META[result.outcome] || { color: 'default', icon: null } : null;

  return (
    <>
      <PageHeader
        title="Obrada dolaska"
        subtitle="Unesite ili skenirajte kod propusnice, ličnog bedža stanara ili osoblja"
      />

      <Box sx={{ display: 'flex', flexDirection: { xs: 'column', md: 'row' }, gap: 3 }}>
        <Paper sx={{ p: 3, flex: 1, maxWidth: 560 }}>
          <ErrorAlert message={error} />
          <Stack spacing={2}>
            <TextField
              label="Kod propusnice / bedža"
              value={code}
              onChange={(e) => setCode(e.target.value)}
              onKeyDown={(e) => {
                if (e.key === 'Enter') submitScan();
              }}
              autoFocus
              fullWidth
              placeholder="npr. GP-9T6CFDXM ili RES-XXXXXXXX"
            />
            <Stack direction="row" spacing={2}>
              <Button
                variant="contained"
                startIcon={loading ? <CircularProgress size={18} color="inherit" /> : <SendIcon />}
                onClick={() => submitScan()}
                disabled={loading || !code.trim()}
                fullWidth
              >
                Proveri kod
              </Button>
              <Button
                variant="outlined"
                startIcon={<QrCodeScannerIcon />}
                onClick={() => setCameraOpen((v) => !v)}
              >
                {cameraOpen ? 'Zatvori kameru' : 'Skeniraj kamerom'}
              </Button>
            </Stack>

            <Collapse in={cameraOpen}>
              <Box sx={{ mt: 1 }}>
                <QrScannerPanel onScan={handleCameraScan} />
              </Box>
            </Collapse>

            <Divider />

            <Button color="error" variant="outlined" startIcon={<BlockIcon />} onClick={() => setDenyOpen((v) => !v)}>
              Ručno odbij ulazak
            </Button>
            <Collapse in={denyOpen}>
              <Stack spacing={1.5} sx={{ pt: 1 }}>
                <Typography variant="body2" color="text.secondary">
                  Odbijanje i pored eventualno validnog koda — razlog je obavezan.
                </Typography>
                <TextField
                  label="Razlog odbijanja"
                  value={denyReason}
                  onChange={(e) => setDenyReason(e.target.value)}
                  required
                  fullWidth
                  multiline
                  minRows={2}
                />
                <Button
                  color="error"
                  variant="contained"
                  onClick={handleDeny}
                  disabled={denyLoading || !denyReason.trim()}
                >
                  Potvrdi odbijanje
                </Button>
              </Stack>
            </Collapse>
          </Stack>
        </Paper>

        <Box sx={{ flex: 1 }}>
          {result && (
            <Paper
              sx={{
                p: 3,
                borderLeft: '6px solid',
                borderColor: `${meta.color}.main`,
              }}
            >
              <Stack direction="row" spacing={2} alignItems="center" sx={{ mb: 1 }}>
                <Box sx={{ color: `${meta.color}.main` }}>{meta.icon}</Box>
                <Typography variant="h6">{result.message}</Typography>
              </Stack>
              <Stack spacing={1} sx={{ mt: 2 }}>
                {result.personName && <Row label="Ime" value={result.personName} />}
                {result.personType && <Row label="Tip lica" value={result.personType} />}
                {result.gatePassCode && <Row label="Kod propusnice" value={result.gatePassCode} />}
                {formatTime(result.entryTime) && <Row label="Vreme ulaska" value={formatTime(result.entryTime)} />}
                {formatTime(result.exitTime) && <Row label="Vreme izlaska" value={formatTime(result.exitTime)} />}
              </Stack>
            </Paper>
          )}
          {!result && (
            <Alert severity="info" variant="outlined">
              Rezultat provere koda će se prikazati ovde.
            </Alert>
          )}
        </Box>
      </Box>
    </>
  );
}

function Row({ label, value }) {
  return (
    <Stack direction="row" spacing={1}>
      <Typography variant="body2" color="text.secondary" sx={{ minWidth: 130 }}>
        {label}
      </Typography>
      <Typography variant="body2" fontWeight={600}>
        {value}
      </Typography>
    </Stack>
  );
}
