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
  Box,
  Tabs,
  Tab,
} from '@mui/material';
import SearchIcon from '@mui/icons-material/Search';
import { getTodayDenials, searchDenials } from '../../api/denials';
import { getBuildings } from '../../api/buildings';
import { extractErrorMessage } from '../../api/client';
import { useAuth } from '../../context/AuthContext';
import LoadingBox from '../../components/common/LoadingBox';
import ErrorAlert from '../../components/common/ErrorAlert';
import PageHeader from '../../components/common/PageHeader';
import { DenialReasonChip } from '../../components/common/StatusChip';

const REASON_OPTIONS = [
  { value: '', label: 'Svi razlozi' },
  { value: 'INVALID_CODE', label: 'Nevalidan kod' },
  { value: 'EXPIRED', label: 'Istekla propusnica' },
  { value: 'USED_UP', label: 'Iskorišćena propusnica' },
  { value: 'CANCELED', label: 'Otkazana propusnica' },
  { value: 'MANUAL_DENIAL', label: 'Ručno odbijanje' },
];

function formatDateTime(value) {
  if (!value) return '—';
  return new Date(value).toLocaleString('sr-RS');
}

function DenialsTable({ rows }) {
  return (
    <Table>
      <TableHead>
        <TableRow>
          <TableCell>Vreme pokušaja</TableCell>
          <TableCell>Uneti kod</TableCell>
          <TableCell>Ime lica</TableCell>
          <TableCell>Razlog</TableCell>
          <TableCell>Napomena</TableCell>
          <TableCell>Zgrada</TableCell>
          <TableCell>Obradio</TableCell>
        </TableRow>
      </TableHead>
      <TableBody>
        {rows.map((r) => (
          <TableRow key={r.id} hover>
            <TableCell>{formatDateTime(r.attemptTime)}</TableCell>
            <TableCell sx={{ fontFamily: 'monospace' }}>{r.enteredCode || '—'}</TableCell>
            <TableCell>{r.personName || '—'}</TableCell>
            <TableCell>
              <DenialReasonChip value={r.reasonType} />
            </TableCell>
            <TableCell>{r.reasonNote || '—'}</TableCell>
            <TableCell>{r.buildingName}</TableCell>
            <TableCell>{r.processedByName || '—'}</TableCell>
          </TableRow>
        ))}
        {rows.length === 0 && (
          <TableRow>
            <TableCell colSpan={7} align="center" sx={{ py: 4, color: 'text.secondary' }}>
              Nema odbijenih pokušaja.
            </TableCell>
          </TableRow>
        )}
      </TableBody>
    </Table>
  );
}

export default function DenialsPage() {
  const { user } = useAuth();
  const isAdmin = user?.role === 'ADMIN';

  const [tab, setTab] = useState(0);
  const [buildings, setBuildings] = useState([]);
  const [buildingId, setBuildingId] = useState('');
  const [todayRows, setTodayRows] = useState([]);

  const [reasonType, setReasonType] = useState('');
  const [text, setText] = useState('');
  const [from, setFrom] = useState('');
  const [to, setTo] = useState('');
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [sort, setSort] = useState('attemptTime,desc');
  const [searchResult, setSearchResult] = useState({ content: [], totalElements: 0 });

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    if (isAdmin) {
      getBuildings()
        .then((data) => {
          setBuildings(data);
          if (data.length > 0) setBuildingId(String(data[0].id));
        })
        .catch((err) => setError(extractErrorMessage(err)));
    }
  }, [isAdmin]);

  const effectiveBuildingId = isAdmin ? buildingId : undefined;

  const loadToday = useCallback(() => {
    if (isAdmin && !buildingId) return;
    setLoading(true);
    getTodayDenials(effectiveBuildingId)
      .then(setTodayRows)
      .catch((err) => setError(extractErrorMessage(err)))
      .finally(() => setLoading(false));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [effectiveBuildingId, isAdmin, buildingId]);

  const loadSearch = useCallback(() => {
    if (isAdmin && !buildingId) return;
    setLoading(true);
    setError('');
    searchDenials({
      buildingId: effectiveBuildingId,
      reasonType: reasonType || undefined,
      from: from || undefined,
      to: to || undefined,
      text: text || undefined,
      page,
      size,
      sort,
    })
      .then(setSearchResult)
      .catch((err) => setError(extractErrorMessage(err)))
      .finally(() => setLoading(false));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [effectiveBuildingId, isAdmin, buildingId, reasonType, from, to, text, page, size, sort]);

  useEffect(() => {
    setError('');
    if (tab === 0) loadToday();
    if (tab === 1) loadSearch();
  }, [tab, loadToday, loadSearch]);

  return (
    <>
      <PageHeader
        title="Odbijeni pokušaji ulaska"
        subtitle={isAdmin ? 'Pregled odbijenih pokušaja za sve zgrade' : 'Odbijeni pokušaji ulaska za vašu zgradu'}
      />
      <ErrorAlert message={error} />

      {isAdmin && (
        <Box sx={{ mb: 2, maxWidth: 320 }}>
          <TextField select label="Zgrada" value={buildingId} onChange={(e) => setBuildingId(e.target.value)} fullWidth>
            {buildings.map((b) => (
              <MenuItem key={b.id} value={String(b.id)}>
                {b.name}
              </MenuItem>
            ))}
          </TextField>
        </Box>
      )}

      <Tabs value={tab} onChange={(_, v) => setTab(v)} sx={{ mb: 2 }}>
        <Tab label="Danas" />
        <Tab label="Pretraga" />
      </Tabs>

      {tab === 1 && (
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
              label="Razlog"
              value={reasonType}
              onChange={(e) => setReasonType(e.target.value)}
              sx={{ minWidth: 200 }}
              size="small"
            >
              {REASON_OPTIONS.map((o) => (
                <MenuItem key={o.value} value={o.value}>
                  {o.label}
                </MenuItem>
              ))}
            </TextField>
            <TextField label="Pretraga po imenu/kodu" value={text} onChange={(e) => setText(e.target.value)} size="small" />
            <TextField
              label="Od"
              type="datetime-local"
              value={from}
              onChange={(e) => setFrom(e.target.value)}
              size="small"
              slotProps={{ inputLabel: { shrink: true } }}
            />
            <TextField
              label="Do"
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
              <MenuItem value="attemptTime,desc">Vreme pokušaja (najnovije)</MenuItem>
              <MenuItem value="attemptTime,asc">Vreme pokušaja (najstarije)</MenuItem>
            </TextField>
            <Button
              variant="contained"
              startIcon={<SearchIcon />}
              onClick={() => {
                setPage(0);
                loadSearch();
              }}
            >
              Pretraži
            </Button>
          </Stack>
        </Paper>
      )}

      <Paper>
        {loading ? (
          <LoadingBox />
        ) : tab === 0 ? (
          <DenialsTable rows={todayRows} />
        ) : (
          <>
            <DenialsTable rows={searchResult.content} />
            <TablePagination
              component="div"
              count={searchResult.totalElements}
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
