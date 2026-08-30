import { useEffect, useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
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
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import VisibilityIcon from '@mui/icons-material/Visibility';
import { getMyGatePasses } from '../../api/gatepasses';
import { extractErrorMessage } from '../../api/client';
import LoadingBox from '../../components/common/LoadingBox';
import ErrorAlert from '../../components/common/ErrorAlert';
import PageHeader from '../../components/common/PageHeader';
import { GatePassStatusChip, GatePassTypeChip } from '../../components/common/StatusChip';

function formatDateTime(value) {
  if (!value) return '—';
  return new Date(value).toLocaleString('sr-RS');
}

export default function MyGatePassesPage() {
  const navigate = useNavigate();
  const [data, setData] = useState({ content: [], totalElements: 0 });
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const load = useCallback(() => {
    setLoading(true);
    getMyGatePasses(page, size, 'createdAt,desc')
      .then(setData)
      .catch((err) => setError(extractErrorMessage(err)))
      .finally(() => setLoading(false));
  }, [page, size]);

  useEffect(() => {
    load();
  }, [load]);

  return (
    <>
      <PageHeader
        title="Moje propusnice"
        subtitle="Pregled statusa i istorije svih propusnica koje ste kreirali za goste"
        action={
          <Button variant="contained" startIcon={<AddIcon />} onClick={() => navigate('/resident/gatepasses/new')}>
            Nova propusnica
          </Button>
        }
      />
      <ErrorAlert message={error} />
      <Paper>
        {loading ? (
          <LoadingBox />
        ) : (
          <>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>Kod</TableCell>
                  <TableCell>Gost</TableCell>
                  <TableCell>Tip</TableCell>
                  <TableCell>Važi od</TableCell>
                  <TableCell>Važi do</TableCell>
                  <TableCell>Ulasci</TableCell>
                  <TableCell>Status</TableCell>
                  <TableCell align="right">Akcije</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {data.content.map((p) => (
                  <TableRow key={p.id} hover>
                    <TableCell sx={{ fontFamily: 'monospace' }}>{p.code}</TableCell>
                    <TableCell>{p.guestName}</TableCell>
                    <TableCell>
                      <GatePassTypeChip value={p.type} />
                    </TableCell>
                    <TableCell>{formatDateTime(p.validFrom)}</TableCell>
                    <TableCell>{formatDateTime(p.validTo)}</TableCell>
                    <TableCell>
                      {p.usedEntries}/{p.maxEntries}
                    </TableCell>
                    <TableCell>
                      <GatePassStatusChip value={p.status} />
                    </TableCell>
                    <TableCell align="right">
                      <Tooltip title="Detalji">
                        <IconButton size="small" onClick={() => navigate(`/resident/gatepasses/${p.id}`)}>
                          <VisibilityIcon fontSize="small" />
                        </IconButton>
                      </Tooltip>
                    </TableCell>
                  </TableRow>
                ))}
                {data.content.length === 0 && (
                  <TableRow>
                    <TableCell colSpan={8} align="center" sx={{ py: 4, color: 'text.secondary' }}>
                      Nemate kreiranih propusnica.
                    </TableCell>
                  </TableRow>
                )}
              </TableBody>
            </Table>
            <TablePagination
              component="div"
              count={data.totalElements}
              page={page}
              onPageChange={(_, p) => setPage(p)}
              rowsPerPage={size}
              onRowsPerPageChange={(e) => {
                setSize(parseInt(e.target.value, 10));
                setPage(0);
              }}
              rowsPerPageOptions={[5, 10, 25]}
              labelRowsPerPage="Redova po strani"
              labelDisplayedRows={({ from, to, count }) => `${from}–${to} od ${count}`}
            />
          </>
        )}
      </Paper>
    </>
  );
}
