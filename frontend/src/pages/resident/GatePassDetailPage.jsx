import { useCallback, useEffect, useState } from 'react';
import { useLocation, useNavigate, useParams } from 'react-router-dom';
import {
  Paper,
  Box,
  Typography,
  Stack,
  Divider,
  Button,
  Alert,
  Table,
  TableHead,
  TableRow,
  TableCell,
  TableBody,
  TextField,
  IconButton,
  Tooltip,
} from '@mui/material';
import EditIcon from '@mui/icons-material/Edit';
import CancelIcon from '@mui/icons-material/Cancel';
import ContentCopyIcon from '@mui/icons-material/ContentCopy';
import { cancelGatePass, getGatePassHistory, getMyGatePass } from '../../api/gatepasses';
import { extractErrorMessage } from '../../api/client';
import LoadingBox from '../../components/common/LoadingBox';
import ErrorAlert from '../../components/common/ErrorAlert';
import PageHeader from '../../components/common/PageHeader';
import QrCodeImage from '../../components/common/QrCodeImage';
import { GatePassStatusChip, GatePassTypeChip } from '../../components/common/StatusChip';
import ConfirmDialog from '../../components/common/ConfirmDialog';

function formatDateTime(value) {
  if (!value) return '—';
  return new Date(value).toLocaleString('sr-RS');
}

export default function GatePassDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const location = useLocation();
  const [pass, setPass] = useState(null);
  const [history, setHistory] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [confirmOpen, setConfirmOpen] = useState(false);
  const [canceling, setCanceling] = useState(false);
  const [message, setMessage] = useState(location.state?.message || '');

  const load = useCallback(() => {
    setLoading(true);
    Promise.all([getMyGatePass(id), getGatePassHistory(id)])
      .then(([p, h]) => {
        setPass(p);
        setHistory(h);
      })
      .catch((err) => setError(extractErrorMessage(err)))
      .finally(() => setLoading(false));
  }, [id]);

  useEffect(() => {
    load();
  }, [load]);

  const handleCancel = async () => {
    setCanceling(true);
    try {
      await cancelGatePass(id);
      setConfirmOpen(false);
      setMessage('Propusnica je otkazana!');
      load();
    } catch (err) {
      setError(extractErrorMessage(err));
      setConfirmOpen(false);
    } finally {
      setCanceling(false);
    }
  };

  const shareUrl = pass ? `${window.location.origin}/pass/${pass.code}` : '';

  if (loading) return <LoadingBox />;

  return (
    <>
      <PageHeader
        title="Detalji propusnice"
        action={
          pass?.status === 'ACTIVE' && (
            <Stack direction="row" spacing={1}>
              <Button
                startIcon={<EditIcon />}
                onClick={() => navigate(`/resident/gatepasses/${id}/edit`)}
              >
                Izmeni
              </Button>
              <Button color="error" startIcon={<CancelIcon />} onClick={() => setConfirmOpen(true)}>
                Otkaži propusnicu
              </Button>
            </Stack>
          )
        }
      />
      <ErrorAlert message={error} />
      {message && (
        <Alert severity="success" sx={{ mb: 2 }} onClose={() => setMessage('')}>
          {message}
        </Alert>
      )}

      {pass && (
        <Stack spacing={3}>
          <Paper sx={{ p: 3 }}>
            <Box
              sx={{
                display: 'flex',
                flexDirection: { xs: 'column', md: 'row' },
                gap: 4,
                alignItems: { xs: 'center', md: 'flex-start' },
              }}
            >
              <QrCodeImage base64={pass.qrCodeBase64} size={200} />
              <Stack spacing={1.5} sx={{ width: '100%' }}>
                <Stack direction="row" spacing={1} sx={{
                  alignItems: "center"
                }}>
                  <Typography variant="h6" sx={{
                    fontFamily: "monospace"
                  }}>
                    {pass.code}
                  </Typography>
                  <GatePassStatusChip value={pass.status} />
                  <GatePassTypeChip value={pass.type} />
                </Stack>
                <Row label="Gost" value={pass.guestName} />
                <Row label="Telefon" value={pass.guestPhone} />
                <Row label="Email" value={pass.guestEmail} />
                <Row label="Razlog dolaska" value={pass.reason} />
                <Row label="Važi od" value={formatDateTime(pass.validFrom)} />
                <Row label="Važi do" value={formatDateTime(pass.validTo)} />
                <Row label="Iskorišćeno ulazaka" value={`${pass.usedEntries} / ${pass.maxEntries}`} />
                <Divider />
                <Typography variant="caption" sx={{
                  color: "text.secondary"
                }}>
                  Javni link za deljenje gostu
                </Typography>
                <Stack direction="row" spacing={1}>
                  <TextField value={shareUrl} size="small" fullWidth slotProps={{ input: { readOnly: true } }} />
                  <Tooltip title="Kopiraj link">
                    <IconButton onClick={() => navigator.clipboard?.writeText(shareUrl)}>
                      <ContentCopyIcon fontSize="small" />
                    </IconButton>
                  </Tooltip>
                </Stack>
              </Stack>
            </Box>
          </Paper>

          <Paper sx={{ p: 3 }}>
            <Typography
              variant="subtitle1"
              sx={{
                fontWeight: 700,
                mb: 2
              }}>
              Istorija statusa
            </Typography>
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>Prethodni status</TableCell>
                  <TableCell>Novi status</TableCell>
                  <TableCell>Vreme izmene</TableCell>
                  <TableCell>Izmenio</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {history.map((h) => (
                  <TableRow key={h.id}>
                    <TableCell>
                      {h.previousStatus ? <GatePassStatusChip value={h.previousStatus} /> : '—'}
                    </TableCell>
                    <TableCell>
                      <GatePassStatusChip value={h.newStatus} />
                    </TableCell>
                    <TableCell>{formatDateTime(h.changedAt)}</TableCell>
                    <TableCell>{h.changedByName || 'Sistem'}</TableCell>
                  </TableRow>
                ))}
                {history.length === 0 && (
                  <TableRow>
                    <TableCell colSpan={4} align="center" sx={{ py: 3, color: 'text.secondary' }}>
                      Nema zabeleženih izmena statusa.
                    </TableCell>
                  </TableRow>
                )}
              </TableBody>
            </Table>
          </Paper>
        </Stack>
      )}

      <ConfirmDialog
        open={confirmOpen}
        title="Otkazivanje propusnice"
        message="Da li ste sigurni da želite da otkažete ovu propusnicu? Ova radnja se ne može opozvati."
        confirmLabel="Otkaži propusnicu"
        confirmColor="error"
        onConfirm={handleCancel}
        onClose={() => setConfirmOpen(false)}
        loading={canceling}
      />
    </>
  );
}

function Row({ label, value }) {
  if (!value) return null;
  return (
    <Stack direction="row" spacing={1}>
      <Typography
        variant="body2"
        sx={{
          color: "text.secondary",
          minWidth: 140
        }}>
        {label}
      </Typography>
      <Typography variant="body2" sx={{
        fontWeight: 600
      }}>
        {value}
      </Typography>
    </Stack>
  );
}
