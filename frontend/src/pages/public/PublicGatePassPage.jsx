import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { Box, Paper, Typography, Stack, Divider } from '@mui/material';
import SecurityIcon from '@mui/icons-material/Security';
import { getPublicGatePass } from '../../api/gatepasses';
import { extractErrorMessage } from '../../api/client';
import LoadingBox from '../../components/common/LoadingBox';
import ErrorAlert from '../../components/common/ErrorAlert';
import QrCodeImage from '../../components/common/QrCodeImage';
import { GatePassStatusChip } from '../../components/common/StatusChip';

function formatDateTime(value) {
  if (!value) return '—';
  return new Date(value).toLocaleString('sr-RS');
}

export default function PublicGatePassPage() {
  const { code } = useParams();
  const [pass, setPass] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    setLoading(true);
    getPublicGatePass(code)
      .then(setPass)
      .catch((err) => setError(extractErrorMessage(err)))
      .finally(() => setLoading(false));
  }, [code]);

  return (
    <Box
      sx={{
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        minHeight: "100vh",
        bgcolor: 'background.default',
        p: 2
      }}>
      <Paper elevation={2} sx={{ p: 4, maxWidth: 460, width: '100%' }}>
        <Stack
          spacing={1}
          sx={{
            alignItems: "center",
            mb: 2
          }}>
          <SecurityIcon color="primary" sx={{ fontSize: 36 }} />
          <Typography
            variant="h6"
            sx={{
              fontWeight: 700,
              textAlign: "center"
            }}>
            Propusnica za posetu
          </Typography>
        </Stack>

        {loading && <LoadingBox />}
        <ErrorAlert message={error} />

        {pass && !loading && (
          <Stack spacing={2} sx={{
            alignItems: "center"
          }}>
            <QrCodeImage base64={pass.qrCodeBase64} size={200} />
            <Typography variant="h6" sx={{
              fontFamily: "monospace"
            }}>
              {pass.code}
            </Typography>
            <GatePassStatusChip value={pass.status} />
            <Divider flexItem />
            <Stack spacing={1} sx={{
              width: "100%"
            }}>
              <Row label="Gost" value={pass.guestName} />
              <Row label="Razlog dolaska" value={pass.reason} />
              <Row label="Zgrada" value={pass.buildingName} />
              <Row label="Važi od" value={formatDateTime(pass.validFrom)} />
              <Row label="Važi do" value={formatDateTime(pass.validTo)} />
            </Stack>
            <Typography
              variant="caption"
              sx={{
                color: "text.secondary",
                textAlign: "center"
              }}>
              Pokažite ovaj kod ili QR obezbeđenju prilikom dolaska u zgradu.
            </Typography>
          </Stack>
        )}
      </Paper>
    </Box>
  );
}

function Row({ label, value }) {
  return (
    <Stack direction="row" sx={{
      justifyContent: "space-between"
    }}>
      <Typography variant="body2" sx={{
        color: "text.secondary"
      }}>
        {label}
      </Typography>
      <Typography variant="body2" sx={{
        fontWeight: 600
      }}>
        {value || '—'}
      </Typography>
    </Stack>
  );
}
