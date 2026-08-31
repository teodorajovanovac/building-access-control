import { useCallback, useEffect, useState } from 'react';
import {
  Paper,
  Box,
  TextField,
  MenuItem,
  Stack,
  Button,
  Typography,
} from '@mui/material';
import SearchIcon from '@mui/icons-material/Search';
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip as RechartsTooltip,
  Legend,
  ResponsiveContainer,
} from 'recharts';
import { getStatistics } from '../../api/statistics';
import { getBuildings } from '../../api/buildings';
import { getSecurityMe } from '../../api/access';
import { extractErrorMessage } from '../../api/client';
import { useAuth } from '../../context/AuthContext';
import LoadingBox from '../../components/common/LoadingBox';
import ErrorAlert from '../../components/common/ErrorAlert';
import PageHeader from '../../components/common/PageHeader';

function isoDate(d) {
  return d.toISOString().slice(0, 10);
}

function StatCard({ label, value, color = 'primary.main' }) {
  return (
    <Paper sx={{ p: 2.5, flex: '1 1 200px', minWidth: 180 }}>
      <Typography variant="body2" sx={{
        color: "text.secondary"
      }}>
        {label}
      </Typography>
      <Typography
        variant="h4"
        sx={{
          fontWeight: 700,
          color
        }}>
        {value}
      </Typography>
    </Paper>
  );
}

export default function StatisticsPage() {
  const { user } = useAuth();
  const isAdmin = user?.role === 'ADMIN';

  const [buildings, setBuildings] = useState([]);
  const [buildingId, setBuildingId] = useState('');
  const [buildingName, setBuildingName] = useState('');
  const [from, setFrom] = useState(() => {
    const d = new Date();
    d.setDate(d.getDate() - 6);
    return isoDate(d);
  });
  const [to, setTo] = useState(() => isoDate(new Date()));

  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (isAdmin) {
      getBuildings()
        .then((data) => {
          setBuildings(data);
          if (data.length > 0) setBuildingId(String(data[0].id));
        })
        .catch((err) => setError(extractErrorMessage(err)));
    } else {
      getSecurityMe()
        .then((data) => {
          setBuildingId(String(data.buildingId));
          setBuildingName(data.buildingName);
        })
        .catch((err) => setError(extractErrorMessage(err)));
    }
  }, [isAdmin]);

  const load = useCallback(() => {
    if (!buildingId) return;
    setLoading(true);
    setError('');
    getStatistics(buildingId, from, to)
      .then(setStats)
      .catch((err) => setError(extractErrorMessage(err)))
      .finally(() => setLoading(false));
  }, [buildingId, from, to]);

  useEffect(() => {
    if (buildingId) load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isAdmin && buildingId]);

  return (
    <>
      <PageHeader title="Statistika posećenosti" subtitle="Broj propusnica, ulazaka i odbijenih pokušaja po zgradi i periodu" />
      <ErrorAlert message={error} />

      <Paper sx={{ p: 2, mb: 3 }}>
        <Stack
          direction="row"
          spacing={2}
          useFlexGap
          sx={{
            flexWrap: "wrap",
            alignItems: "center"
          }}>
          {isAdmin ? (
            <TextField
              select
              label="Zgrada"
              value={buildingId}
              onChange={(e) => setBuildingId(e.target.value)}
              sx={{ minWidth: 200 }}
              size="small"
            >
              {buildings.map((b) => (
                <MenuItem key={b.id} value={String(b.id)}>
                  {b.name}
                </MenuItem>
              ))}
            </TextField>
          ) : (
            <Typography variant="body1" sx={{ minWidth: 140 }}>
              Zgrada: <strong>{buildingName || '…'}</strong>
            </Typography>
          )}
          <TextField
            label="Od"
            type="date"
            value={from}
            onChange={(e) => setFrom(e.target.value)}
            size="small"
            slotProps={{ inputLabel: { shrink: true } }}
          />
          <TextField
            label="Do"
            type="date"
            value={to}
            onChange={(e) => setTo(e.target.value)}
            size="small"
            slotProps={{ inputLabel: { shrink: true } }}
          />
          <Button variant="contained" startIcon={<SearchIcon />} onClick={load} disabled={!buildingId}>
            Prikaži
          </Button>
        </Stack>
      </Paper>

      {loading && <LoadingBox />}

      {stats && !loading && (
        <Stack spacing={3}>
          <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 2 }}>
            <StatCard label="Ukupno ulazaka" value={stats.totalEntries} />
            <StatCard label="Ukupno propusnica" value={stats.totalGatePasses} />
            <StatCard label="Odbijeni pokušaji" value={stats.totalDeniedAttempts} color="error.main" />
            <StatCard label="Trenutno prisutnih" value={stats.currentlyPresentCount} color="success.main" />
          </Box>

          <Paper sx={{ p: 3 }}>
            <Typography
              variant="subtitle1"
              sx={{
                fontWeight: 700,
                mb: 2
              }}>
              Ulasci i odbijeni pokušaji po danu — {stats.buildingName}
            </Typography>
            <Box sx={{ width: '100%', height: 320 }}>
              <ResponsiveContainer>
                <BarChart data={stats.dailyBreakdown}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="date" />
                  <YAxis allowDecimals={false} />
                  <RechartsTooltip />
                  <Legend />
                  <Bar dataKey="entryCount" name="Ulasci" fill="#2f5fb3" radius={[4, 4, 0, 0]} />
                  <Bar dataKey="denialCount" name="Odbijeni pokušaji" fill="#d32f2f" radius={[4, 4, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            </Box>
          </Paper>
        </Stack>
      )}
    </>
  );
}
