import { Box } from '@mui/material';

/** Prikazuje QR kod koji backend generiše on-the-fly kao raw base64 PNG (bez data: prefiksa). */
export default function QrCodeImage({ base64, alt = 'QR kod', size = 180 }) {
  if (!base64) return null;
  return (
    <Box
      component="img"
      src={`data:image/png;base64,${base64}`}
      alt={alt}
      sx={{
        width: size,
        height: size,
        border: '1px solid',
        borderColor: 'divider',
        borderRadius: 1,
        p: 1,
        backgroundColor: '#fff',
      }}
    />
  );
}
