import { useCallback, useEffect, useMemo, useState } from 'react';
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
import QrCode2Icon from '@mui/icons-material/QrCode2';
import { createUser, deleteUser, getUsers, updateUser } from '../../api/users';
import { getBuildings } from '../../api/buildings';
import { extractErrorMessage } from '../../api/client';
import LoadingBox from '../../components/common/LoadingBox';
import ErrorAlert from '../../components/common/ErrorAlert';
import PageHeader from '../../components/common/PageHeader';
import ConfirmDialog from '../../components/common/ConfirmDialog';
import QrCodeImage from '../../components/common/QrCodeImage';

const EMPTY_FORM = { firstName: '', lastName: '', email: '', password: '', jobTitle: '' };

export default function StaffPage() {
  const [buildings, setBuildings] = useState([]);
  const [buildingId, setBuildingId] = useState('');
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const [dialogOpen, setDialogOpen] = useState(false);
  const [editing, setEditing] = useState(null);
  const [form, setForm] = useState(EMPTY_FORM);
  const [saving, setSaving] = useState(false);
  const [formError, setFormError] = useState('');

  const [badgeView, setBadgeView] = useState(null);
  const [deleteTarget, setDeleteTarget] = useState(null);
  const [deleting, setDeleting] = useState(false);

  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);

  useEffect(() => {
    getBuildings()
      .then((data) => {
        setBuildings(data);
        if (data.length > 0) setBuildingId(String(data[0].id));
      })
      .catch((err) => setError(extractErrorMessage(err)));
  }, []);

  const load = useCallback(() => {
    setLoading(true);
    getUsers()
      .then(setUsers)
      .catch((err) => setError(extractErrorMessage(err)))
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  const staff = useMemo(
    () => users.filter((u) => u.role === 'STAFF' && (!buildingId || String(u.buildingId) === String(buildingId))),
    [users, buildingId]
  );

  useEffect(() => {
    setPage(0);
  }, [buildingId]);

  const openCreate = () => {
    setEditing(null);
    setForm(EMPTY_FORM);
    setFormError('');
    setDialogOpen(true);
  };

  const openEdit = (u) => {
    setEditing(u);
    setForm({
      firstName: u.firstName,
      lastName: u.lastName,
      email: u.email,
      password: '',
      jobTitle: u.jobTitle || '',
    });
    setFormError('');
    setDialogOpen(true);
  };

  const handleSave = async () => {
    setSaving(true);
    setFormError('');
    try {
      if (editing) {
        await updateUser(editing.id, {
          firstName: form.firstName,
          lastName: form.lastName,
          apartmentId: null,
          buildingId: Number(buildingId),
          jobTitle: form.jobTitle,
        });
        setDialogOpen(false);
        load();
      } else {
        const created = await createUser({
          firstName: form.firstName,
          lastName: form.lastName,
          email: form.email,
          password: form.password,
          role: 'STAFF',
          buildingId: Number(buildingId),
          jobTitle: form.jobTitle,
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
      await deleteUser(deleteTarget.id);
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
        subtitle="Osoblje (npr. održavanje, recepcija) — nalog sa prijavom i automatski dodeljenim bedž kodom, kao i stanari"
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
                <TableCell>Email</TableCell>
                <TableCell>Uloga / opis</TableCell>
                <TableCell>Bedž kod</TableCell>
                <TableCell align="right">Akcije</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {staff.slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage).map((s) => (
                <TableRow key={s.id} hover>
                  <TableCell>
                    {s.firstName} {s.lastName}
                  </TableCell>
                  <TableCell>{s.email}</TableCell>
                  <TableCell>{s.jobTitle || '—'}</TableCell>
                  <TableCell sx={{ fontFamily: 'monospace' }}>{s.badgeCode || '—'}</TableCell>
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
        <TablePagination
          component="div"
          count={staff.length}
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
        <DialogTitle>{editing ? 'Izmena člana osoblja' : 'Novi član osoblja'}</DialogTitle>
        <DialogContent>
          <ErrorAlert message={formError} />
          <Stack spacing={2} sx={{ mt: 1 }}>
            <Stack direction="row" spacing={2}>
              <TextField
                label="Ime"
                value={form.firstName}
                onChange={(e) => setForm((f) => ({ ...f, firstName: e.target.value }))}
                required
                fullWidth
                autoFocus
              />
              <TextField
                label="Prezime"
                value={form.lastName}
                onChange={(e) => setForm((f) => ({ ...f, lastName: e.target.value }))}
                required
                fullWidth
              />
            </Stack>
            {!editing && (
              <>
                <TextField
                  label="Email"
                  type="email"
                  value={form.email}
                  onChange={(e) => setForm((f) => ({ ...f, email: e.target.value }))}
                  required
                  fullWidth
                />
                <TextField
                  label="Lozinka"
                  type="password"
                  value={form.password}
                  onChange={(e) => setForm((f) => ({ ...f, password: e.target.value }))}
                  helperText="Najmanje 6 karaktera"
                  required
                  fullWidth
                />
              </>
            )}
            <TextField
              label="Uloga / opis (npr. Održavanje)"
              value={form.jobTitle}
              onChange={(e) => setForm((f) => ({ ...f, jobTitle: e.target.value }))}
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
            disabled={
              saving ||
              !form.firstName ||
              !form.lastName ||
              (!editing && (!form.email || !form.password))
            }
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
              <Box color="text.secondary">
                {badgeView?.firstName} {badgeView?.lastName}
              </Box>
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
        message={`Da li ste sigurni da želite da obrišete "${deleteTarget?.firstName} ${deleteTarget?.lastName}"?`}
        confirmLabel="Obriši"
        confirmColor="error"
        onConfirm={handleDelete}
        onClose={() => setDeleteTarget(null)}
        loading={deleting}
      />
    </>
  );
}
