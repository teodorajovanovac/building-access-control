import { useCallback, useEffect, useState } from 'react';
import {
  Paper,
  Table,
  TableHead,
  TableRow,
  TableCell,
  TableBody,
  Button,
  IconButton,
  Tooltip,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  MenuItem,
  Stack,
  CircularProgress,
  Box,
  Chip,
  Switch,
  FormControlLabel,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import QrCode2Icon from '@mui/icons-material/QrCode2';
import { createStaff, deleteStaff, getStaffByBuilding, updateStaff } from '../../api/staff';
import { getBuildings } from '../../api/buildings';
import { extractErrorMessage } from '../../api/client';
import LoadingBox from '../../components/common/LoadingBox';
import ErrorAlert from '../../components/common/ErrorAlert';
import PageHeader from '../../components/common/PageHeader';
import ConfirmDialog from '../../components/common/ConfirmDialog';
import QrCodeImage from '../../components/common/QrCodeImage';

export default function StaffPage() {
  const [buildings, setBuildings] = useState([]);
  const [buildingId, setBuildingId] = useState('');
  const [staff, setStaff] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const [dialogOpen, setDialogOpen] = useState(false);
  const [editing, setEditing] = useState(null);
  const [form, setForm] = useState({ fullName: '', jobTitle: '', active: true });
  const [saving, setSaving] = useState(false);
  const [formError, setFormError] = useState('');

  const [badgeView, setBadgeView] = useState(null);
  const [deleteTarget, setDeleteTarget] = useState(null);
  const [deleting, setDeleting] = useState(false);

  useEffect(() => {
    getBuildings()
      .then((data) => {
        setBuildings(data);
        if (data.length > 0) setBuildingId(String(data[0].id));
      })
      .catch((err) => setError(extractErrorMessage(err)));
  }, []);

  const load = useCallback(() => {
    if (!buildingId) return;
    setLoading(true);
    getStaffByBuilding(buildingId)
      .then(setStaff)
      .catch((err) => setError(extractErrorMessage(err)))
      .finally(() => setLoading(false));
  }, [buildingId]);

  useEffect(() => {
    load();
  }, [load]);

  const openCreate = () => {
    setEditing(null);
    setForm({ fullName: '', jobTitle: '', active: true });
    setFormError('');
    setDialogOpen(true);
  };

  const openEdit = (s) => {
    setEditing(s);
    setForm({ fullName: s.fullName, jobTitle: s.jobTitle, active: s.active });
    setFormError('');
    setDialogOpen(true);
  };

  const handleSave = async () => {
    setSaving(true);
    setFormError('');
    try {
      if (editing) {
        await updateStaff(editing.id, form);
        setDialogOpen(false);
        load();
      } else {
        const created = await createStaff({
          fullName: form.fullName,
          jobTitle: form.jobTitle,
          buildingId: Number(buildingId),
        });
        setDialogOpen(false);
        load();
        setBadgeView(created);
      }
    } catch (err) {
      setFormError(extractErrorMessage(err));
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async () => {
    setDeleting(true);
    try {
      await deleteStaff(deleteTarget.id);
      setDeleteTarget(null);
      load();
    } catch (err) {
      setError(extractErrorMessage(err));
      setDeleteTarget(null);
    } finally {
      setDeleting(false);
    }
  };

  return (
    <>
      <PageHeader
        title="Osoblje zgrade"
        subtitle="Osoblje (npr. održavanje) sa ličnim bedž kodom za ulazak — bez korisničkog naloga"
        action={
          <Button variant="contained" startIcon={<AddIcon />} onClick={openCreate} disabled={!buildingId}>
            Novi član osoblja
          </Button>
        }
      />
      <ErrorAlert message={error} />

      <Box sx={{ mb: 2, maxWidth: 320 }}>
        <TextField select label="Zgrada" value={buildingId} onChange={(e) => setBuildingId(e.target.value)} fullWidth>
          {buildings.map((b) => (
            <MenuItem key={b.id} value={String(b.id)}>
              {b.name}
            </MenuItem>
          ))}
        </TextField>
      </Box>

      <Paper>
        {loading ? (
          <LoadingBox />
        ) : (
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Ime</TableCell>
                <TableCell>Uloga / opis</TableCell>
                <TableCell>Bedž kod</TableCell>
                <TableCell>Status</TableCell>
                <TableCell align="right">Akcije</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {staff.map((s) => (
                <TableRow key={s.id} hover>
                  <TableCell>{s.fullName}</TableCell>
                  <TableCell>{s.jobTitle}</TableCell>
                  <TableCell sx={{ fontFamily: 'monospace' }}>{s.badgeCode}</TableCell>
                  <TableCell>
                    <Chip
                      label={s.active ? 'Aktivan' : 'Deaktiviran'}
                      color={s.active ? 'success' : 'default'}
                      size="small"
                    />
                  </TableCell>
                  <TableCell align="right">
                    <Tooltip title="Prikaži bedž/QR">
                      <IconButton size="small" onClick={() => setBadgeView(s)}>
                        <QrCode2Icon fontSize="small" />
                      </IconButton>
                    </Tooltip>
                    <Tooltip title="Izmeni">
                      <IconButton size="small" onClick={() => openEdit(s)}>
                        <EditIcon fontSize="small" />
                      </IconButton>
                    </Tooltip>
                    <Tooltip title="Obriši">
                      <IconButton size="small" color="error" onClick={() => setDeleteTarget(s)}>
                        <DeleteIcon fontSize="small" />
                      </IconButton>
                    </Tooltip>
                  </TableCell>
                </TableRow>
              ))}
              {staff.length === 0 && (
                <TableRow>
                  <TableCell colSpan={5} align="center" sx={{ py: 4, color: 'text.secondary' }}>
                    Nema osoblja u odabranoj zgradi.
                  </TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        )}
      </Paper>

      <Dialog open={dialogOpen} onClose={() => setDialogOpen(false)} maxWidth="xs" fullWidth>
        <DialogTitle>{editing ? 'Izmena člana osoblja' : 'Novi član osoblja'}</DialogTitle>
        <DialogContent>
          <ErrorAlert message={formError} />
          <Stack spacing={2} sx={{ mt: 1 }}>
            <TextField
              label="Ime i prezime"
              value={form.fullName}
              onChange={(e) => setForm((f) => ({ ...f, fullName: e.target.value }))}
              required
              fullWidth
              autoFocus
            />
            <TextField
              label="Uloga / opis (npr. Održavanje)"
              value={form.jobTitle}
              onChange={(e) => setForm((f) => ({ ...f, jobTitle: e.target.value }))}
              required
              fullWidth
            />
            {editing && (
              <FormControlLabel
                control={
                  <Switch
                    checked={form.active}
                    onChange={(e) => setForm((f) => ({ ...f, active: e.target.checked }))}
                  />
                }
                label="Aktivan"
              />
            )}
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDialogOpen(false)} disabled={saving}>
            Otkaži
          </Button>
          <Button
            variant="contained"
            onClick={handleSave}
            disabled={saving || !form.fullName || !form.jobTitle}
            startIcon={saving ? <CircularProgress size={16} color="inherit" /> : null}
          >
            Sačuvaj
          </Button>
        </DialogActions>
      </Dialog>

      <Dialog open={Boolean(badgeView)} onClose={() => setBadgeView(null)} maxWidth="xs" fullWidth>
        <DialogTitle>Bedž kod za ulazak</DialogTitle>
        <DialogContent>
          <Stack spacing={2} alignItems="center" sx={{ py: 1 }}>
            <QrCodeImage base64={badgeView?.qrCodeBase64} size={200} />
            <Box textAlign="center">
              <Box fontWeight={700} fontFamily="monospace" fontSize={18}>
                {badgeView?.badgeCode}
              </Box>
              <Box color="text.secondary">{badgeView?.fullName}</Box>
            </Box>
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setBadgeView(null)}>Zatvori</Button>
        </DialogActions>
      </Dialog>

      <ConfirmDialog
        open={Boolean(deleteTarget)}
        title="Brisanje člana osoblja"
        message={`Da li ste sigurni da želite da obrišete "${deleteTarget?.fullName}"?`}
        confirmLabel="Obriši"
        confirmColor="error"
        onConfirm={handleDelete}
        onClose={() => setDeleteTarget(null)}
        loading={deleting}
      />
    </>
  );
}
