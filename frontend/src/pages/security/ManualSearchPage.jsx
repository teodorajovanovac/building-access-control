import { useState } from 'react';
import {
  Paper,
  Box,
  TextField,
  Button,
  Stack,
  Table,
  TableHead,
  TableRow,
  TableCell,
  TableBody,
  Chip,
  CircularProgress,
} from '@mui/material';
import SearchIcon from '@mui/icons-material/Search';
import LoginIcon from '@mui/icons-material/Login';
import { searchPeople, processManual } from '../../api/access';
import { extractErrorMessage } from '../../api/client';
import PageHeader from '../../components/common/PageHeader';
import ErrorAlert from '../../components/common/ErrorAlert';
import LoadingBox from '../../components/common/LoadingBox';
import { PersonTypeChip } from '../../components/common/StatusChip';

export default function ManualSearchPage() {
  const [query, setQuery] = useState('');
  const [results, setResults] = useState(null);
  const [loading, setLoading] = useState(false);
  const [processingId, setProcessingId] = useState(null);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');

  const handleSearch = async (e) => {
    e?.preventDefault();
    if (!query.trim()) return;
    setLoading(true);
    setError('');
    setMessage('');
    try {
      const res = await searchPeople(query.trim());
      setResults(res);
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  const handleProcess = async (person) => {
    setProcessingId(person.id);
    setError('');
    setMessage('');
    try {
      const res = await processManual(person.personType, person.id);
      setMessage(res.message);
      handleSearch();
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setProcessingId(null);
    }
  };

  return (
    <>
      <PageHeader
        title="Ručna pretraga stanara i osoblja"
        subtitle="Rezervna opcija kada lice nema svoj kod/QR pri ruci — pretraga po imenu ili broju stana"
      />

      <Paper sx={{ p: 3, mb: 3 }}>
        <Box component="form" onSubmit={handleSearch}>
          <Stack direction="row" spacing={2}>
            <TextField
              label="Ime, prezime ili broj stana"
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              fullWidth
              autoFocus
            />
            <Button
              type="submit"
              variant="contained"
              startIcon={loading ? <CircularProgress size={18} color="inherit" /> : <SearchIcon />}
              disabled={loading || !query.trim()}
            >
              Pretraži
            </Button>
          </Stack>
        </Box>
      </Paper>

      <ErrorAlert message={error} />
      {message && (
        <Box sx={{ mb: 2 }}>
          <Chip label={message} color="success" />
        </Box>
      )}

      {loading && <LoadingBox />}

      {results && !loading && (
        <Paper>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Tip</TableCell>
                <TableCell>Ime</TableCell>
                <TableCell>Stan</TableCell>
                <TableCell>Uloga/opis</TableCell>
                <TableCell>Trenutno u zgradi</TableCell>
                <TableCell align="right">Akcija</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {results.map((p) => (
                <TableRow key={`${p.personType}-${p.id}`} hover>
                  <TableCell>
                    <PersonTypeChip value={p.personType} />
                  </TableCell>
                  <TableCell>{p.fullName}</TableCell>
                  <TableCell>{p.apartmentNumber || '—'}</TableCell>
                  <TableCell>{p.jobTitle || '—'}</TableCell>
                  <TableCell>
                    {p.currentlyInBuilding ? (
                      <Chip label="U zgradi" color="success" size="small" variant="outlined" />
                    ) : (
                      <Chip label="Van zgrade" size="small" variant="outlined" />
                    )}
                  </TableCell>
                  <TableCell align="right">
                    <Button
                      size="small"
                      variant="outlined"
                      startIcon={
                        processingId === p.id ? <CircularProgress size={16} /> : <LoginIcon fontSize="small" />
                      }
                      disabled={processingId === p.id}
                      onClick={() => handleProcess(p)}
                    >
                      Evidentiraj
                    </Button>
                  </TableCell>
                </TableRow>
              ))}
              {results.length === 0 && (
                <TableRow>
                  <TableCell colSpan={6} align="center" sx={{ py: 4, color: 'text.secondary' }}>
                    Nema rezultata za uneti pojam.
                  </TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        </Paper>
      )}
    </>
  );
}
