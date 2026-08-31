import { useCallback, useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import {
  Paper,
  Table,
  TableHead,
  TableRow,
  TableCell,
  TableBody,
  TablePagination,
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
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import { createApartment, deleteApartment, getApartmentsByBuilding, updateApartment } from '../../api/apartments';
import { getBuildings } from '../../api/buildings';
import { extractErrorMessage } from '../../api/client';
import LoadingBox from '../../components/common/LoadingBox';
import ErrorAlert from '../../components/common/ErrorAlert';
import PageHeader from '../../components/common/PageHeader';
import ConfirmDialog from '../../components/common/ConfirmDialog';

export default function ApartmentsPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const [buildings, setBuildings] = useState([]);
  const [buildingId, setBuildingId] = useState(searchParams.get('buildingId') || '');
  const [apartments, setApartments] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const [dialogOpen, setDialogOpen] = useState(false);
  const [editing, setEditing] = useState(null);
  const [form, setForm] = useState({ number: '', floor: 0 });
  const [saving, setSaving] = useState(false);
  const [formError, setFormError] = useState('');

  const [deleteTarget, setDeleteTarget] = useState(null);
  const [deleting, setDeleting] = useState(false);

  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);

  useEffect(() => {
    getBuildings()
      .then((data) => {
        setBuildings(data);
        if (!buildingId && data.length > 0) {
          setBuildingId(String(data[0].id));
        }
      })
      .catch((err) => setError(extractErrorMessage(err)));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const load = useCallback(() => {
    if (!buildingId) return;
    setLoading(true);
    getApartmentsByBuilding(buildingId)
      .then(setApartments)
      .catch((err) => setError(extractErrorMessage(err)))
      .finally(() => setLoading(false));
  }, [buildingId]);

  useEffect(() => {
    load();
    setPage(0);
    if (buildingId) setSearchParams({ buildingId });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [load]);

  const openCreate = () => {
    setEditing(null);
    setForm({ number: '', floor: 0 });
    setFormError('');
    setDialogOpen(true);
  };

  const openEdit = (a) => {
    setEditing(a);
    setForm({ number: a.number, floor: a.floor });
    setFormError('');
    setDialogOpen(true);
  };

  const handleSave = async () => {
    setSaving(true);
    setFormError('');
    try {
      const payload = { number: form.number, floor: Number(form.floor), buildingId: Number(buildingId) };
      if (editing) {
        await updateApartment(editing.id, payload);
      } else {
        await createApartment(payload);
      }
      setDialogOpen(false);
      load();
    } catch (err) {
      setFormError(extractErrorMessage(err));
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async () => {
    setDeleting(true);
    try {
      await deleteApartment(deleteTarget.id);
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
        title="Stanovi"
        subtitle="Upravljanje stanovima po zgradi"
        action={
          <Button variant="contained" startIcon={<AddIcon />} onClick={openCreate} disabled={!buildingId}>
            Novi stan
          </Button>
        }
      />
      <ErrorAlert message={error} />

      <Box sx={{ mb: 2, maxWidth: 320 }}>
        <TextField
          select
          label="Zgrada"
          value={buildingId}
          onChange={(e) => setBuildingId(e.target.value)}
          fullWidth
        >
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
                <TableCell>Broj stana</TableCell>
                <TableCell>Sprat</TableCell>
                <TableCell>Broj stanara</TableCell>
                <TableCell align="right">Akcije</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {apartments.slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage).map((a) => (
                <TableRow key={a.id} hover>
                  <TableCell>{a.number}</TableCell>
                  <TableCell>{a.floor}</TableCell>
                  <TableCell>{a.residentCount}</TableCell>
                  <TableCell align="right">
                    <Tooltip title="Izmeni">
                      <IconButton size="small" onClick={() => openEdit(a)}>
                        <EditIcon fontSize="small" />
                      </IconButton>
                    </Tooltip>
                    <Tooltip title="Obriši">
                      <IconButton size="small" color="error" onClick={() => setDeleteTarget(a)}>
                        <DeleteIcon fontSize="small" />
                      </IconButton>
                    </Tooltip>
                  </TableCell>
                </TableRow>
              ))}
              {apartments.length === 0 && (
                <TableRow>
                  <TableCell colSpan={4} align="center" sx={{ py: 4, color: 'text.secondary' }}>
                    Nema stanova u odabranoj zgradi.
                  </TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        )}
        <TablePagination
          component="div"
          count={apartments.length}
          page={page}
          onPageChange={(_, p) => setPage(p)}
          rowsPerPage={rowsPerPage}
          onRowsPerPageChange={(e) => {
            setRowsPerPage(parseInt(e.target.value, 10));
            setPage(0);
          }}
          rowsPerPageOptions={[10, 25, 50]}
          labelRowsPerPage="Redova po strani"
          labelDisplayedRows={({ from: f, to: t, count }) => `${f}–${t} od ${count}`}
        />
      </Paper>

      <Dialog open={dialogOpen} onClose={() => setDialogOpen(false)} maxWidth="xs" fullWidth>
        <DialogTitle>{editing ? 'Izmena stana' : 'Novi stan'}</DialogTitle>
        <DialogContent>
          <ErrorAlert message={formError} />
          <Stack spacing={2} sx={{ mt: 1 }}>
            <TextField
              label="Broj stana"
              value={form.number}
              onChange={(e) => setForm((f) => ({ ...f, number: e.target.value }))}
              required
              fullWidth
              autoFocus
            />
            <TextField
              label="Sprat"
              type="number"
              value={form.floor}
              onChange={(e) => setForm((f) => ({ ...f, floor: e.target.value }))}
              required
              fullWidth
            />
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDialogOpen(false)} disabled={saving}>
            Otkaži
          </Button>
          <Button
            variant="contained"
            onClick={handleSave}
            disabled={saving || !form.number}
            startIcon={saving ? <CircularProgress size={16} color="inherit" /> : null}
          >
            Sačuvaj
          </Button>
        </DialogActions>
      </Dialog>

      <ConfirmDialog
        open={Boolean(deleteTarget)}
        title="Brisanje stana"
        message={`Da li ste sigurni da želite da obrišete stan "${deleteTarget?.number}"?`}
        confirmLabel="Obriši"
        confirmColor="error"
        onConfirm={handleDelete}
        onClose={() => setDeleteTarget(null)}
        loading={deleting}
      />
    </>
  );
}
