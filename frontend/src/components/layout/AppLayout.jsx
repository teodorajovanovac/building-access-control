import { useState } from 'react';
import { Outlet, NavLink, useNavigate } from 'react-router-dom';
import {
  AppBar,
  Toolbar,
  Typography,
  Drawer,
  List,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Box,
  IconButton,
  Avatar,
  Menu,
  MenuItem,
  Divider,
  useMediaQuery,
} from '@mui/material';
import MenuIcon from '@mui/icons-material/Menu';
import ApartmentIcon from '@mui/icons-material/Apartment';
import BadgeIcon from '@mui/icons-material/Badge';
import ConfirmationNumberIcon from '@mui/icons-material/ConfirmationNumber';
import ListAltIcon from '@mui/icons-material/ListAlt';
import QrCodeScannerIcon from '@mui/icons-material/QrCodeScanner';
import PersonSearchIcon from '@mui/icons-material/PersonSearch';
import BlockIcon from '@mui/icons-material/Block';
import BarChartIcon from '@mui/icons-material/BarChart';
import HomeWorkIcon from '@mui/icons-material/HomeWork';
import GroupIcon from '@mui/icons-material/Group';
import EngineeringIcon from '@mui/icons-material/Engineering';
import HistoryIcon from '@mui/icons-material/History';
import LogoutIcon from '@mui/icons-material/Logout';
import SecurityIcon from '@mui/icons-material/Security';
import { useAuth } from '../../context/AuthContext';

const DRAWER_WIDTH = 250;

const NAV_ITEMS = {
  RESIDENT: [
    { to: '/resident/profile', label: 'Moj profil', icon: <BadgeIcon /> },
    { to: '/resident/gatepasses', label: 'Moje propusnice', icon: <ConfirmationNumberIcon /> },
    { to: '/resident/entrylogs', label: 'Evidencija ulazaka', icon: <ListAltIcon /> },
  ],
  SECURITY: [
    { to: '/security/scan', label: 'Obrada dolaska', icon: <QrCodeScannerIcon /> },
    { to: '/security/search', label: 'Ručna pretraga', icon: <PersonSearchIcon /> },
    { to: '/security/logs', label: 'Dnevna evidencija', icon: <HistoryIcon /> },
    { to: '/security/denials', label: 'Odbijeni pokušaji', icon: <BlockIcon /> },
    { to: '/security/statistics', label: 'Statistika', icon: <BarChartIcon /> },
  ],
  ADMIN: [
    { to: '/admin/buildings', label: 'Zgrade', icon: <HomeWorkIcon /> },
    { to: '/admin/apartments', label: 'Stanovi', icon: <ApartmentIcon /> },
    { to: '/admin/users', label: 'Korisnici', icon: <GroupIcon /> },
    { to: '/admin/staff', label: 'Osoblje zgrade', icon: <EngineeringIcon /> },
    { to: '/admin/gatepasses', label: 'Propusnice', icon: <ConfirmationNumberIcon /> },
    { to: '/admin/entrylogs', label: 'Evidencija ulazaka', icon: <ListAltIcon /> },
    { to: '/admin/denials', label: 'Odbijeni pokušaji', icon: <BlockIcon /> },
    { to: '/admin/statistics', label: 'Statistika', icon: <BarChartIcon /> },
  ],
};

const ROLE_TITLES = {
  RESIDENT: 'Stanar',
  SECURITY: 'Obezbeđenje',
  ADMIN: 'Administrator',
};

export default function AppLayout() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const isSmall = useMediaQuery((t) => t.breakpoints.down('md'));
  const [mobileOpen, setMobileOpen] = useState(false);
  const [anchorEl, setAnchorEl] = useState(null);

  const items = NAV_ITEMS[user?.role] || [];

  const handleLogout = () => {
    setAnchorEl(null);
    logout();
    navigate('/login', { replace: true });
  };

  const drawerContent = (
    <Box sx={{ display: 'flex', flexDirection: 'column', height: '100%' }}>
      <Toolbar>
        <SecurityIcon color="primary" sx={{ mr: 1 }} />
        <Typography variant="subtitle1" noWrap sx={{
          fontWeight: 700
        }}>
          Kontrola pristupa
        </Typography>
      </Toolbar>
      <Divider />
      <List sx={{ flexGrow: 1 }}>
        {items.map((item) => (
          <ListItemButton
            key={item.to}
            component={NavLink}
            to={item.to}
            onClick={() => setMobileOpen(false)}
            sx={{
              '&.active': {
                backgroundColor: 'primary.main',
                color: 'primary.contrastText',
                '& .MuiListItemIcon-root': { color: 'primary.contrastText' },
                '&:hover': { backgroundColor: 'primary.dark' },
              },
              mx: 1,
              borderRadius: 1,
              mb: 0.5,
            }}
          >
            <ListItemIcon>{item.icon}</ListItemIcon>
            <ListItemText primary={item.label} />
          </ListItemButton>
        ))}
      </List>
    </Box>
  );

  return (
    <Box sx={{ display: 'flex', minHeight: '100vh' }}>
      <AppBar
        position="fixed"
        color="inherit"
        elevation={0}
        sx={{
          width: { md: `calc(100% - ${DRAWER_WIDTH}px)` },
          ml: { md: `${DRAWER_WIDTH}px` },
          borderBottom: '1px solid',
          borderColor: 'divider',
        }}
      >
        <Toolbar sx={{ display: 'flex', justifyContent: 'space-between' }}>
          <Box sx={{ display: 'flex', alignItems: 'center' }}>
            {isSmall && (
              <IconButton edge="start" onClick={() => setMobileOpen(true)} sx={{ mr: 1 }}>
                <MenuIcon />
              </IconButton>
            )}
            <Typography variant="h6" noWrap>
              {ROLE_TITLES[user?.role] || ''}
            </Typography>
          </Box>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <Typography
              variant="body2"
              sx={{
                color: "text.secondary",
                display: { xs: 'none', sm: 'block' }
              }}>
              {user?.firstName} {user?.lastName}
            </Typography>
            <IconButton onClick={(e) => setAnchorEl(e.currentTarget)}>
              <Avatar sx={{ width: 34, height: 34, bgcolor: 'primary.main' }}>
                {user?.firstName?.[0]?.toUpperCase() || '?'}
              </Avatar>
            </IconButton>
            <Menu anchorEl={anchorEl} open={Boolean(anchorEl)} onClose={() => setAnchorEl(null)}>
              <MenuItem disabled>{user?.email}</MenuItem>
              <Divider />
              <MenuItem onClick={handleLogout}>
                <ListItemIcon>
                  <LogoutIcon fontSize="small" />
                </ListItemIcon>
                Odjava
              </MenuItem>
            </Menu>
          </Box>
        </Toolbar>
      </AppBar>

      <Box component="nav" sx={{ width: { md: DRAWER_WIDTH }, flexShrink: { md: 0 } }}>
        <Drawer
          variant={isSmall ? 'temporary' : 'permanent'}
          open={isSmall ? mobileOpen : true}
          onClose={() => setMobileOpen(false)}
          ModalProps={{ keepMounted: true }}
          sx={{
            '& .MuiDrawer-paper': {
              width: DRAWER_WIDTH,
              boxSizing: 'border-box',
              borderRight: '1px solid',
              borderColor: 'divider',
            },
          }}
        >
          {drawerContent}
        </Drawer>
      </Box>

      <Box
        component="main"
        sx={{
          flexGrow: 1,
          width: { md: `calc(100% - ${DRAWER_WIDTH}px)` },
          bgcolor: 'background.default',
          minHeight: '100vh',
        }}
      >
        <Toolbar />
        <Box sx={{ p: { xs: 2, md: 3 } }}>
          <Outlet />
        </Box>
      </Box>
    </Box>
  );
}
