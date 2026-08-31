import { Box, Stack, Typography } from '@mui/material';

export default function PageHeader({ title, subtitle, action }) {
  return (
    <Stack
      direction={{ xs: 'column', sm: 'row' }}
      spacing={2}
      sx={{
        justifyContent: "space-between",
        alignItems: { xs: 'flex-start', sm: 'center' },
        mb: 3
      }}>
      <Box>
        <Typography variant="h5">{title}</Typography>
        {subtitle && (
          <Typography variant="body2" sx={{
            color: "text.secondary"
          }}>
            {subtitle}
          </Typography>
        )}
      </Box>
      {action && <Box>{action}</Box>}
    </Stack>
  );
}
