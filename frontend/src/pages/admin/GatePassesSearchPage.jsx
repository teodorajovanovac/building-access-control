import { useCallback, useEffect, useState } from 'react';
import {
  Paper,
  Table,
  TableHead,
  TableRow,
  TableCell,
  TableBody,
  TablePagination,
  TextField,
  MenuItem,
  Stack,
  Button,
} from '@mui/material';
import SearchIcon from '@mui/icons-material/Search';
import { searchGatePasses } from '../../api/gatepasses';
import { getBuildings } from '../../api/buildings';
import { extractErrorMessage } from '../../api/client';
import LoadingBox from '../../components/common/LoadingBox';
import ErrorAlert from '../../components/common/ErrorAlert';
import PageHeader from '../../components/common/PageHeader';
import { GatePassStatusChip, GatePassTypeChip } from '../../components/common/StatusChip';

const STATUS_OPTIONS = [
  { value: '', label: 'Svi statusi' },
  { value: 'ACTIVE', label: 'Aktivna' },
  { value: 'USED_UP', label: 'Iskorišćena' },
  { value: 'EXPIRED', label: 'Istekla' },
  { value: 'CANCELED', label: 'Otkazana' },
];

function formatDateTime(value) {
  if (!value) return '—';
  return new Date(value).toLocaleString('sr-RS');
}

export default function GatePassesSearchPage() {
  const [buildings, setBuildings] = useState([]);
  const [buildingId, setBuildingId] = useState('');
  const [status, setStatus] = useState('');
  const [text, setText] = useState('');
  const [from, setFrom] = useState('');
  const [to, setTo] = useState('');
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [sort, setSort] = useState('createdAt,desc');
  const [result, setResult] = useState({ content: [], totalElements: 0 });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    getBuildings()
      .then(setBuildings)
      .catch((err) => setError(extractErrorMessage(err)));
  }, []);

  const load = useCallback(() => {
    setLoading(true);
    setError('');
    searchGatePasses({
      buildingId: buildingId || undefined,
      status: status || undefined,
      from: from || undefined,
      to: to || undefined,
      text: text || undefined,
      page,
      size,
      sort,
    })
      .then(setResult)
      .catch((err) => setError(extractErrorMessage(err)))
      .finally(() => setLoading(false));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [buildingId, status, from, to, text, page, size, sort]);

  useEffect(() => {
    load();
  }, [load]);

  return (
    <>
      <PageHeader title="Propusnice" subtitle="Pretraga, filtriranje, sortiranje i paginacija svih propusnica" />
      <ErrorAlert message={error} />

      <Paper sx={{ p: 2, mb: 2 }}>
        <Stack
          direction="row"
          spacing={2}
          useFlexGap
          sx={{
            flexWrap: "wrap",
            alignItems: "center"
          }}>
          <TextField
            select
            label="Zgrada"
            value={buildingId}
            onChange={(e) => setBuildingId(e.target.value)}
            sx={{ minWidth: 180 }}
            size="small"
          >
            <MenuItem value="">Sve zgrade</MenuItem>
            {buildings.map((b) => (
              <MenuItem key={b.id} value={String(b.id)}>
                {b.name}
              </MenuItem>
            ))}
          </TextField>
          <TextField
            select
            label="Status"
            value={status}
            onChange={(e) => setStatus(e.target.value)}
            sx={{ minWidth: 160 }}
            size="small"
          >
            {STATUS_OPTIONS.map((o) => (
              <MenuItem key={o.value} value={o.value}>
                {o.label}
              </MenuItem>
            ))}
          </TextField>
          <TextField
            label="Pretraga (gost/kod)"
            value={text}
            onChange={(e) => setText(e.target.value)}
            size="small"
          />
          <TextField
            label="Kreirana od"
            type="datetime-local"
            value={from}
            onChange={(e) => setFrom(e.target.value)}
            size="small"
            slotProps={{ inputLabel: { shrink: true } }}
          />
          <TextField
            label="Kreirana do"
            type="datetime-local"
            value={to}
            onChange={(e) => setTo(e.target.value)}
            size="small"
            slotProps={{ inputLabel: { shrink: true } }}
          />
          <TextField
            select
            label="Sortiraj po"
            value={sort}
            onChange={(e) => setSort(e.target.value)}
            size="small"
            sx={{ minWidth: 200 }}
          >
            <MenuItem value="createdAt,desc">Kreirano (najnovije)</MenuItem>
            <MenuItem value="createdAt,asc">Kreirano (najstarije)</MenuItem>
            <MenuItem value="validTo,asc">Ističe uskoro</MenuItem>
            <MenuItem value="guestName,asc">Gost (A–Š)</MenuItem>
          </TextField>
          <Button
            variant="contained"
            startIcon={<SearchIcon />}
            onClick={() => {
              setPage(0);
              load();
            }}
          >
            Pretraži
          </Button>
        </Stack>
      </Paper>

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
                  <TableCell>Stan</TableCell>
                  <TableCell>Zgrada</TableCell>
                  <TableCell>Tip</TableCell>
                  <TableCell>Ulasci</TableCell>
                  <TableCell>Status</TableCell>
                  <TableCell>Kreirana</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {result.content.map((p) => (
                  <TableRow key={p.id} hover>
                    <TableCell sx={{ fontFamily: 'monospace' }}>{p.code}</TableCell>
                    <TableCell>{p.guestName}</TableCell>
                    <TableCell>{p.apartmentNumber}</TableCell>
                    <TableCell>{p.buildingName}</TableCell>
                    <TableCell>
                      <GatePassTypeChip value={p.type} />
                    </TableCell>
                    <TableCell>
                      {p.usedEntries}/{p.maxEntries}
                    </TableCell>
                    <TableCell>
                      <GatePassStatusChip value={p.status} />
                    </TableCell>
                    <TableCell>{formatDateTime(p.createdAt)}</TableCell>
                  </TableRow>
                ))}
                {result.content.length === 0 && (
                  <TableRow>
                    <TableCell colSpan={8} align="center" sx={{ py: 4, color: 'text.secondary' }}>
                      Nema rezultata.
                    </TableCell>
                  </TableRow>
                )}
              </TableBody>
            </Table>
            <TablePagination
              component="div"
              count={result.totalElements}
              page={page}
              onPageChange={(_, p) => setPage(p)}
              rowsPerPage={size}
              onRowsPerPageChange={(e) => {
                setSize(parseInt(e.target.value, 10));
                setPage(0);
              }}
              rowsPerPageOptions={[10, 25, 50]}
              labelRowsPerPage="Redova po strani"
              labelDisplayedRows={({ from: f, to: t, count }) => `${f}–${t} od ${count}`}
            />
          </>
        )}
      </Paper>
    </>
  );
}
