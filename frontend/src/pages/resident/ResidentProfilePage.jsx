import { useEffect, useState } from 'react';
import { Paper, Typography, Stack, Divider, Box } from '@mui/material';
import { getMyProfile } from '../../api/users';
import { extractErrorMessage } from '../../api/client';
import LoadingBox from '../../components/common/LoadingBox';
import ErrorAlert from '../../components/common/ErrorAlert';
import QrCodeImage from '../../components/common/QrCodeImage';
import PageHeader from '../../components/common/PageHeader';

function Field({ label, value }) {
  return (
    <Stack spacing={0.25}>
      <Typography variant="caption" sx={{
        color: "text.secondary"
      }}>
        {label}
      </Typography>
      <Typography variant="body1" sx={{
        fontWeight: 600
      }}>
        {value || '—'}
      </Typography>
    </Stack>
  );
}

export default function ResidentProfilePage() {
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    getMyProfile()
      .then(setProfile)
      .catch((err) => setError(extractErrorMessage(err)))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <LoadingBox />;

  return (
    <>
      <PageHeader title="Moj profil" subtitle="Vaš lični bedž kod i QR kod za ulazak u zgradu" />
      <ErrorAlert message={error} />
      {profile && (
        <Paper sx={{ p: 3 }}>
          <Box
            sx={{
              display: 'flex',
              flexDirection: { xs: 'column', md: 'row' },
              gap: 4,
              alignItems: { xs: 'center', md: 'flex-start' },
            }}
          >
            <QrCodeImage base64={profile.qrCodeBase64} size={200} />
            <Stack spacing={2} sx={{ width: '100%' }}>
              <Field label="Ime i prezime" value={`${profile.firstName} ${profile.lastName}`} />
              <Field label="Email" value={profile.email} />
              <Divider />
              <Field label="Lični bedž kod" value={profile.badgeCode} />
              <Field label="Stan" value={profile.apartmentNumber} />
              <Field label="Zgrada" value={profile.buildingName} />
            </Stack>
          </Box>
        </Paper>
      )}
    </>
  );
}
