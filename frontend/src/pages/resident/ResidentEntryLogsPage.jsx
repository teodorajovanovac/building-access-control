import { useEffect, useState } from 'react';
import {
  Paper,
  Table,
  TableHead,
  TableRow,
  TableCell,
  TableBody,
  Chip,
} from '@mui/material';
import { getMyApartmentEntryLogs } from '../../api/entrylogs';
import { extractErrorMessage } from '../../api/client';
import LoadingBox from '../../components/common/LoadingBox';
import ErrorAlert from '../../components/common/ErrorAlert';
import PageHeader from '../../components/common/PageHeader';
import { PersonTypeChip } from '../../components/common/StatusChip';

function formatDateTime(value) {
  if (!value) return '—';
  return new Date(value).toLocaleString('sr-RS');
}

export default function ResidentEntryLogsPage() {
  const [logs, setLogs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    getMyApartmentEntryLogs()
      .then(setLogs)
      .catch((err) => setError(extractErrorMessage(err)))
      .finally(() => setLoading(false));
  }, []);

  return (
    <>
      <PageHeader
        title="Evidencija ulazaka"
        subtitle="Ulasci i izlasci povezani sa vašim stanom — vaši lični dolasci i dolasci gostiju preko propusnica"
      />
      <ErrorAlert message={error} />
      <Paper>
        {loading ? (
          <LoadingBox />
        ) : (
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Tip</TableCell>
                <TableCell>Ime</TableCell>
                <TableCell>Ulazak</TableCell>
                <TableCell>Izlazak</TableCell>
                <TableCell>Propusnica</TableCell>
                <TableCell>Napomena</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {logs.map((l) => (
                <TableRow key={l.id} hover>
                  <TableCell>
                    <PersonTypeChip value={l.personType} />
                  </TableCell>
                  <TableCell>{l.personName}</TableCell>
                  <TableCell>{formatDateTime(l.entryTime)}</TableCell>
                  <TableCell>
                    {l.exitTime ? (
                      formatDateTime(l.exitTime)
                    ) : (
                      <Chip label="U zgradi" color="success" size="small" variant="outlined" />
                    )}
                  </TableCell>
                  <TableCell sx={{ fontFamily: 'monospace' }}>{l.gatePassCode || '—'}</TableCell>
                  <TableCell>{l.manualEntry ? 'Ručni unos' : '—'}</TableCell>
                </TableRow>
              ))}
              {logs.length === 0 && (
                <TableRow>
                  <TableCell colSpan={6} align="center" sx={{ py: 4, color: 'text.secondary' }}>
                    Nema zabeleženih ulazaka.
                  </TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        )}
      </Paper>
    </>
  );
}
