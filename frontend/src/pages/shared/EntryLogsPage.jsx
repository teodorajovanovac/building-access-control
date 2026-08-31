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
  Chip,
} from '@mui/material';
import SearchIcon from '@mui/icons-material/Search';
import { getTodayEntryLogs, getCurrentlyPresent, searchEntryLogs } from '../../api/entrylogs';
import { getBuildings } from '../../api/buildings';
import { extractErrorMessage } from '../../api/client';
import { useAuth } from '../../context/AuthContext';
import LoadingBox from '../../components/common/LoadingBox';
import ErrorAlert from '../../components/common/ErrorAlert';
import PageHeader from '../../components/common/PageHeader';
import { PersonTypeChip } from '../../components/common/StatusChip';

const PERSON_TYPE_OPTIONS = [
  { value: '', label: 'Svi' },
  { value: 'GUEST', label: 'Gost' },
  { value: 'RESIDENT', label: 'Stanar' },
  { value: 'STAFF', label: 'Osoblje' },
];

function formatDateTime(value) {
  if (!value) return '—';
  return new Date(value).toLocaleString('sr-RS');
}

function SimpleLogTable({ logs }) {
  return (
    <Table>
      <TableHead>
        <TableRow>
          <TableCell>Tip</TableCell>
          <TableCell>Ime</TableCell>
          <TableCell>Ulazak</TableCell>
          <TableCell>Izlazak</TableCell>
          <TableCell>Zgrada</TableCell>
          <TableCell>Propusnica</TableCell>
          <TableCell>Obradio</TableCell>
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
            <TableCell>{l.buildingName}</TableCell>
            <TableCell sx={{ fontFamily: 'monospace' }}>{l.gatePassCode || '—'}</TableCell>
            <TableCell>{l.processedByName || '—'}</TableCell>
          </TableRow>
        ))}
        {logs.length === 0 && (
          <TableRow>
            <TableCell colSpan={7} align="center" sx={{ py: 4, color: 'text.secondary' }}>
              Nema zapisa.
            </TableCell>
          </TableRow>
        )}
      </TableBody>
    </Table>
  );
}

export default function EntryLogsPage() {
  const { user } = useAuth();
  const isAdmin = user?.role === 'ADMIN';

  const [tab, setTab] = useState(0);
  const [buildings, setBuildings] = useState([]);
  const [buildingId, setBuildingId] = useState('');

  const [todayLogs, setTodayLogs] = useState([]);
  const [presentLogs, setPresentLogs] = useState([]);

  const [personType, setPersonType] = useState('');
  const [text, setText] = useState('');
  const [from, setFrom] = useState('');
  const [to, setTo] = useState('');
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [sort, setSort] = useState('entryTime,desc');
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
    getTodayEntryLogs(effectiveBuildingId)
      .then(setTodayLogs)
      .catch((err) => setError(extractErrorMessage(err)))
      .finally(() => setLoading(false));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [effectiveBuildingId, isAdmin, buildingId]);

  const loadPresent = useCallback(() => {
    if (isAdmin && !buildingId) return;
    setLoading(true);
    getCurrentlyPresent(effectiveBuildingId)
      .then(setPresentLogs)
      .catch((err) => setError(extractErrorMessage(err)))
      .finally(() => setLoading(false));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [effectiveBuildingId, isAdmin, buildingId]);

  const loadSearch = useCallback(() => {
    if (isAdmin && !buildingId) return;
    setLoading(true);
    setError('');
    searchEntryLogs({
      buildingId: effectiveBuildingId,
      personType: personType || undefined,
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
  }, [effectiveBuildingId, isAdmin, buildingId, personType, from, to, text, page, size, sort]);

  useEffect(() => {
    setError('');
    if (tab === 0) loadToday();
    if (tab === 1) loadPresent();
    if (tab === 2) loadSearch();
  }, [tab, loadToday, loadPresent, loadSearch]);

  return (
    <>
      <PageHeader
        title="Evidencija ulazaka"
        subtitle={isAdmin ? 'Evidencija ulazaka i izlazaka za sve zgrade' : 'Evidencija ulazaka i izlazaka za vašu zgradu'}
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
        <Tab label="Trenutno prisutni" />
        <Tab label="Pretraga" />
      </Tabs>

      {tab === 2 && (
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
              label="Tip lica"
              value={personType}
              onChange={(e) => setPersonType(e.target.value)}
              sx={{ minWidth: 160 }}
              size="small"
            >
              {PERSON_TYPE_OPTIONS.map((o) => (
                <MenuItem key={o.value} value={o.value}>
                  {o.label}
                </MenuItem>
              ))}
            </TextField>
            <TextField
              label="Pretraga po imenu"
              value={text}
              onChange={(e) => setText(e.target.value)}
              size="small"
            />
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
              <MenuItem value="entryTime,desc">Vreme ulaska (najnovije)</MenuItem>
              <MenuItem value="entryTime,asc">Vreme ulaska (najstarije)</MenuItem>
              <MenuItem value="personName,asc">Ime (A–Š)</MenuItem>
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
          <SimpleLogTable logs={todayLogs} />
        ) : tab === 1 ? (
          <SimpleLogTable logs={presentLogs} />
        ) : (
          <>
            <SimpleLogTable logs={searchResult.content} />
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
