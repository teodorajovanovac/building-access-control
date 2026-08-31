import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import {
  Paper,
  Box,
  TextField,
  MenuItem,
  Button,
  Stack,
  CircularProgress,
} from '@mui/material';
import SaveIcon from '@mui/icons-material/Save';
import { createGatePass, getMyGatePass, updateGatePass } from '../../api/gatepasses';
import { extractErrorMessage } from '../../api/client';
import ErrorAlert from '../../components/common/ErrorAlert';
import PageHeader from '../../components/common/PageHeader';
import LoadingBox from '../../components/common/LoadingBox';

const TYPE_OPTIONS = [
  { value: 'SINGLE', label: 'Jednokratna (jedan ulazak)' },
  { value: 'LIMITED', label: 'Ograničen broj ulazaka' },
  { value: 'RECURRING', label: 'Ponavljajuća poseta' },
];

function toDatetimeLocal(value) {
  if (!value) return '';
  // backend šalje ISO string (npr. 2026-08-30T14:00:00) — input traži do minuta
  return value.slice(0, 16);
}

export default function GatePassFormPage() {
  const { id } = useParams();
  const isEdit = Boolean(id);
  const navigate = useNavigate();

  const [form, setForm] = useState({
    guestName: '',
    guestPhone: '',
    guestEmail: '',
    reason: '',
    validFrom: '',
    validTo: '',
    maxEntries: 1,
    type: 'SINGLE',
  });
  const [loading, setLoading] = useState(isEdit);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!isEdit) return;
    getMyGatePass(id)
      .then((p) => {
        if (p.status !== 'ACTIVE') {
          setError('Propusnica se više ne može menjati! (nije u statusu ACTIVE)');
        }
        setForm({
          guestName: p.guestName || '',
          guestPhone: p.guestPhone || '',
          guestEmail: p.guestEmail || '',
          reason: p.reason || '',
          validFrom: toDatetimeLocal(p.validFrom),
          validTo: toDatetimeLocal(p.validTo),
          maxEntries: p.maxEntries,
          type: p.type,
        });
      })
      .catch((err) => setError(extractErrorMessage(err)))
      .finally(() => setLoading(false));
  }, [id, isEdit]);

  const update = (field) => (e) => setForm((f) => ({ ...f, [field]: e.target.value }));

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSaving(true);
    try {
      const payload = {
        guestName: form.guestName,
        guestPhone: form.guestPhone || null,
        guestEmail: form.guestEmail || null,
        reason: form.reason,
        validFrom: form.validFrom,
        validTo: form.validTo,
        maxEntries: Number(form.maxEntries),
      };
      if (isEdit) {
        await updateGatePass(id, payload);
        navigate(`/resident/gatepasses/${id}`, { state: { message: 'Propusnica je uspešno izmenjena!' } });
      } else {
        const created = await createGatePass({ ...payload, type: form.type });
        navigate(`/resident/gatepasses/${created.id}`, {
          state: { message: 'Propusnica je uspešno kreirana!' },
        });
      }
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setSaving(false);
    }
  };

  if (loading) return <LoadingBox />;

  return (
    <>
      <PageHeader
        title={isEdit ? 'Izmena propusnice' : 'Nova propusnica za gosta'}
        subtitle={
          isEdit
            ? 'Izmena je moguća samo dok je propusnica u statusu ACTIVE.'
            : 'Unesite podatke o poseti — sistem će generisati jedinstven kod i QR kod.'
        }
      />
      <Paper sx={{ p: 3, maxWidth: 640 }}>
        <ErrorAlert message={error} />
        <Box component="form" onSubmit={handleSubmit} noValidate>
          <Stack spacing={2}>
            <TextField label="Ime gosta" value={form.guestName} onChange={update('guestName')} required fullWidth />
            <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
              <TextField label="Telefon gosta" value={form.guestPhone} onChange={update('guestPhone')} fullWidth />
              <TextField
                label="Email gosta"
                type="email"
                value={form.guestEmail}
                onChange={update('guestEmail')}
                fullWidth
              />
            </Stack>
            <TextField
              label="Razlog dolaska"
              value={form.reason}
              onChange={update('reason')}
              required
              fullWidth
              multiline
              minRows={2}
            />
            <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
              <TextField
                label="Važi od"
                type="datetime-local"
                value={form.validFrom}
                onChange={update('validFrom')}
                required
                fullWidth
                slotProps={{ inputLabel: { shrink: true } }}
              />
              <TextField
                label="Važi do"
                type="datetime-local"
                value={form.validTo}
                onChange={update('validTo')}
                required
                fullWidth
                slotProps={{ inputLabel: { shrink: true } }}
              />
            </Stack>
            <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
              <TextField
                label="Dozvoljen broj ulazaka"
                type="number"
                value={form.maxEntries}
                onChange={update('maxEntries')}
                required
                fullWidth
                slotProps={{ htmlInput: { min: 1 } }}
              />
              {!isEdit && (
                <TextField
                  select
                  label="Tip propusnice"
                  value={form.type}
                  onChange={update('type')}
                  required
                  fullWidth
                >
                  {TYPE_OPTIONS.map((opt) => (
                    <MenuItem key={opt.value} value={opt.value}>
                      {opt.label}
                    </MenuItem>
                  ))}
                </TextField>
              )}
            </Stack>
            <Stack direction="row" spacing={2} sx={{
              justifyContent: "flex-end"
            }}>
              <Button onClick={() => navigate(-1)} disabled={saving}>
                Otkaži
              </Button>
              <Button
                type="submit"
                variant="contained"
                startIcon={saving ? <CircularProgress size={18} color="inherit" /> : <SaveIcon />}
                disabled={saving}
              >
                Sačuvaj
              </Button>
            </Stack>
          </Stack>
        </Box>
      </Paper>
    </>
  );
}
