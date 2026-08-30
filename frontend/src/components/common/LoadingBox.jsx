import { Box, CircularProgress } from '@mui/material';

export default function LoadingBox({ minHeight = 200 }) {
  return (
    <Box display="flex" alignItems="center" justifyContent="center" minHeight={minHeight}>
      <CircularProgress />
    </Box>
  );
}
