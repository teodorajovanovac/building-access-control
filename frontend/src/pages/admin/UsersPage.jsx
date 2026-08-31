import { useCallback, useEffect, useState } from 'react';
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
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import { createUser, deleteUser, getUsers, updateUser } from '../../api/users';
import { getBuildings } from '../../api/buildings';
import { getApartmentsByBuilding } from '../../api/apartments';
import { extractErrorMessage } from '../../api/client';
import LoadingBox from '../../components/common/LoadingBox';
import ErrorAlert from '../../components/common/ErrorAlert';
import PageHeader from '../../components/common/PageHeader';
import ConfirmDialog from '../../components/common/ConfirmDialog';
import { RoleChip } from '../../components/common/StatusChip';

const ROLE_OPTIONS = [
  { value: 'RESIDENT', label: 'Stanar' },
  { value: 'SECURITY', label: 'Obezbeđenje' },
  { value: 'STAFF', label: 'Osoblje zgrade' },
  { value: 'ADMIN', label: 'Administrator' },
];

const EMPTY_FORM = {
  firstName: '',
  lastName: '',
  email: '',
  password: '',
  role: 'RESIDENT',
  buildingIdForApartment: '',
  apartmentId: '',
  buildingId: '',
  jobTitle: '',
};

export default function UsersPage() {
  const [users, setUsers] = useState([]);
  const [buildings, setBuildings] = useState([]);
  const [apartmentOptions, setApartmentOptions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const [dialogOpen, setDialogOpen] = useState(false);
  const [editing, setEditing] = useState(null);
  const [form, setForm] = useState(EMPTY_FORM);
  const [saving, setSaving] = useState(false);
  const [formError, setFormError] = useState('');

  const [deleteTarget, setDeleteTarget] = useState(null);
  const [deleting, setDeleting] = useState(false);

  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);

  const load = useCallback(() => {
    setLoading(true);
    Promise.all([getUsers(), getBuildings()])
      .then(([u, b]) => {
        setUsers(u);
        setBuildings(b);
      })
      .catch((err) => setError(extractErrorMessage(err)))
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  useEffect(() => {
    setPage(0);
  }, [users.length]);

  useEffect(() => {
    if (form.role === 'RESIDENT' && form.buildingIdForApartment) {
      getApartmentsByBuilding(form.buildingIdForApartment)
        .then(setApartmentOptions)
        .catch(() => setApartmentOptions([]));
    } else {
      setApartmentOptions([]);
    }
  }, [form.role, form.buildingIdForApartment]);

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
      role: u.role,
      buildingIdForApartment: u.buildingId ? String(u.buildingId) : '',
      apartmentId: u.apartmentId ? String(u.apartmentId) : '',
      buildingId: u.buildingId ? String(u.buildingId) : '',
      jobTitle: u.jobTitle || '',
    });
    setFormError('');
    setDialogOpen(true);
  };

  const handleSave = async () => {
    setSaving(true);
    setFormError('');
    try {
      const isStaffOrSecurity = form.role === 'SECURITY' || form.role === 'STAFF';
      if (editing) {
        await updateUser(editing.id, {
          firstName: form.firstName,
          lastName: form.lastName,
          apartmentId: form.role === 'RESIDENT' && form.apartmentId ? Number(form.apartmentId) : null,
          buildingId: isStaffOrSecurity && form.buildingId ? Number(form.buildingId) : null,
          jobTitle: form.role === 'STAFF' ? form.jobTitle : null,
        });
      } else {
        await createUser({
          firstName: form.firstName,
          lastName: form.lastName,
          email: form.email,
          password: form.password,
          role: form.role,
          apartmentId: form.role === 'RESIDENT' && form.apartmentId ? Number(form.apartmentId) : null,
          buildingId: isStaffOrSecurity && form.buildingId ? Number(form.buildingId) : null,
          jobTitle: form.role === 'STAFF' ? form.jobTitle : null,
        });
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
        title="Korisnici"
        subtitle="Upravljanje stanarima, obezbeđenjem i administratorima"
        action={
          <Button variant="contained" startIcon={<AddIcon />} onClick={openCreate}>
            Novi korisnik
          </Button>
        }
      />
      <ErrorAlert message={error} />
      <Paper>
        {loading ? (
          <LoadingBox />
        ) : (
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Ime i prezime</TableCell>
                <TableCell>Email</TableCell>
                <TableCell>Uloga</TableCell>
                <TableCell>Bedž kod</TableCell>
                <TableCell>Stan / Zgrada</TableCell>
                <TableCell align="right">Akcije</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {users.slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage).map((u) => (
                <TableRow key={u.id} hover>
                  <TableCell>
                    {u.firstName} {u.lastName}
                  </TableCell>
                  <TableCell>{u.email}</TableCell>
                  <TableCell>
                    <RoleChip value={u.role} />
                  </TableCell>
                  <TableCell sx={{ fontFamily: 'monospace' }}>{u.badgeCode || '—'}</TableCell>
                  <TableCell>
                    {u.role === 'RESIDENT' && (u.apartmentNumber ? `Stan ${u.apartmentNumber} — ${u.buildingName}` : '—')}
                    {(u.role === 'SECURITY' || u.role === 'STAFF') && (u.buildingName || '—')}
                    {u.role === 'ADMIN' && '—'}
                  </TableCell>
                  <TableCell align="right">
                    <Tooltip title="Izmeni">
                      <IconButton size="small" onClick={() => openEdit(u)}>
                        <EditIcon fontSize="small" />
                      </IconButton>
                    </Tooltip>
                    <Tooltip title="Obriši">
                      <IconButton size="small" color="error" onClick={() => setDeleteTarget(u)}>
                        <DeleteIcon fontSize="small" />
                      </IconButton>
                    </Tooltip>
                  </TableCell>
                </TableRow>
              ))}
              {users.length === 0 && (
                <TableRow>
                  <TableCell colSpan={6} align="center" sx={{ py: 4, color: 'text.secondary' }}>
                    Nema korisnika.
                  </TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        )}
        <TablePagination
          component="div"
          count={users.length}
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

      <Dialog open={dialogOpen} onClose={() => setDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>{editing ? 'Izmena korisnika' : 'Novi korisnik'}</DialogTitle>
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
                <TextField
                  select
                  label="Uloga"
                  value={form.role}
                  onChange={(e) => setForm((f) => ({ ...f, role: e.target.value }))}
                  fullWidth
                >
                  {ROLE_OPTIONS.map((r) => (
                    <MenuItem key={r.value} value={r.value}>
                      {r.label}
                    </MenuItem>
                  ))}
                </TextField>
              </>
            )}

            {form.role === 'RESIDENT' && (
              <>
                <TextField
                  select
                  label="Zgrada"
                  value={form.buildingIdForApartment}
                  onChange={(e) =>
                    setForm((f) => ({ ...f, buildingIdForApartment: e.target.value, apartmentId: '' }))
                  }
                  fullWidth
                >
                  {buildings.map((b) => (
                    <MenuItem key={b.id} value={String(b.id)}>
                      {b.name}
                    </MenuItem>
                  ))}
                </TextField>
                <TextField
                  select
                  label="Stan"
                  value={form.apartmentId}
                  onChange={(e) => setForm((f) => ({ ...f, apartmentId: e.target.value }))}
                  disabled={!form.buildingIdForApartment}
                  fullWidth
                >
                  {apartmentOptions.map((a) => (
                    <MenuItem key={a.id} value={String(a.id)}>
                      Stan {a.number}
                    </MenuItem>
                  ))}
                </TextField>
              </>
            )}

            {(form.role === 'SECURITY' || form.role === 'STAFF') && (
              <TextField
                select
                label="Zgrada (raspoređivanje)"
                value={form.buildingId}
                onChange={(e) => setForm((f) => ({ ...f, buildingId: e.target.value }))}
                fullWidth
              >
                {buildings.map((b) => (
                  <MenuItem key={b.id} value={String(b.id)}>
                    {b.name}
                  </MenuItem>
                ))}
              </TextField>
            )}

            {form.role === 'STAFF' && (
              <TextField
                label="Uloga / opis (npr. Održavanje)"
                value={form.jobTitle}
                onChange={(e) => setForm((f) => ({ ...f, jobTitle: e.target.value }))}
                fullWidth
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
            disabled={saving || !form.firstName || !form.lastName}
            startIcon={saving ? <CircularProgress size={16} color="inherit" /> : null}
          >
            Sačuvaj
          </Button>
        </DialogActions>
      </Dialog>

      <ConfirmDialog
        open={Boolean(deleteTarget)}
        title="Brisanje korisnika"
        message={`Da li ste sigurni da želite da obrišete korisnika "${deleteTarget?.firstName} ${deleteTarget?.lastName}"?`}
        confirmLabel="Obriši"
        confirmColor="error"
        onConfirm={handleDelete}
        onClose={() => setDeleteTarget(null)}
        loading={deleting}
      />
    </>
  );
}
