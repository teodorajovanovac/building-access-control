import { useEffect, useRef } from 'react';
import { Box, Typography } from '@mui/material';
import { Html5QrcodeScanner } from 'html5-qrcode';

const ELEMENT_ID = 'qr-scanner-region';

/**
 * Kamera je bonus opcija (SK8/SK9) — ako ne uspe da se inicijalizuje (nema kamere,
 * korisnik odbije dozvolu, http bez tls na mobilnom...), ne sme blokirati tok, jer
 * ručno tekstualno polje za unos koda uvek ostaje dostupno kao osnovna opcija.
 */
export default function QrScannerPanel({ onScan }) {
  const scannerRef = useRef(null);

  useEffect(() => {
    const scanner = new Html5QrcodeScanner(
      ELEMENT_ID,
      { fps: 10, qrbox: { width: 240, height: 240 }, rememberLastUsedCamera: true },
      false,
    );
    scannerRef.current = scanner;

    scanner.render(
      (decodedText) => {
        onScan(decodedText);
      },
      () => {
        // ignorišemo pojedinačne neuspele frejmove skeniranja — normalno je dok se kod ne uhvati
      },
    );

    return () => {
      scanner.clear().catch(() => {});
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    <Box>
      <div id={ELEMENT_ID} />
      <Typography variant="caption" sx={{
        color: "text.secondary"
      }}>
        Ako kamera ne radi na ovom uređaju, unesite kod ručno u polje iznad.
      </Typography>
    </Box>
  );
}
