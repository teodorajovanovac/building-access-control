import { Box, Button, Typography } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import BlockIcon from '@mui/icons-material/Block';

export default function ForbiddenPage() {
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
      <BlockIcon color="error" sx={{ fontSize: 64 }} />
      <Typography variant="h5">Nemate pristup ovoj stranici</Typography>
      <Typography sx={{
        color: "text.secondary"
      }}>Vaša uloga nema dozvolu za ovu funkcionalnost.</Typography>
      <Button variant="contained" onClick={() => navigate('/')}>
        Nazad na početnu
      </Button>
    </Box>
  );
}
