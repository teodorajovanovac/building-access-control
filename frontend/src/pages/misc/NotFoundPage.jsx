import { Box, Button, Typography } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import SearchOffIcon from '@mui/icons-material/SearchOff';

export default function NotFoundPage() {
  const navigate = useNavigate();
  return (
    <Box
      sx={{
        display: "flex",
        flexDirection: "column",
        alignItems: "center",
        justifyContent: "center",
        minHeight: "70vh",
        gap: 2
      }}>
      <SearchOffIcon color="disabled" sx={{ fontSize: 64 }} />
      <Typography variant="h5">Stranica nije pronađena</Typography>
      <Button variant="contained" onClick={() => navigate('/')}>
        Nazad na početnu
      </Button>
    </Box>
  );
}
