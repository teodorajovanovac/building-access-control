import { Chip } from '@mui/material';

const GATE_PASS_STATUS = {
  ACTIVE: { label: 'Aktivna', color: 'success' },
  USED_UP: { label: 'Iskorišćena', color: 'default' },
  EXPIRED: { label: 'Istekla', color: 'warning' },
  CANCELED: { label: 'Otkazana', color: 'error' },
};

const DENIAL_REASON = {
  INVALID_CODE: { label: 'Nevalidan kod', color: 'error' },
  EXPIRED: { label: 'Istekla propusnica', color: 'warning' },
  USED_UP: { label: 'Iskorišćena propusnica', color: 'warning' },
  CANCELED: { label: 'Otkazana propusnica', color: 'error' },
  MANUAL_DENIAL: { label: 'Ručno odbijanje', color: 'error' },
};

const PERSON_TYPE = {
  GUEST: { label: 'Gost', color: 'info' },
  RESIDENT: { label: 'Stanar', color: 'primary' },
  STAFF: { label: 'Osoblje', color: 'secondary' },
};

const ROLE = {
  RESIDENT: { label: 'Stanar', color: 'primary' },
  SECURITY: { label: 'Obezbeđenje', color: 'secondary' },
  ADMIN: { label: 'Administrator', color: 'default' },
};

const GATE_PASS_TYPE = {
  SINGLE: { label: 'Jednokratna', color: 'default' },
  LIMITED: { label: 'Ograničen broj ulazaka', color: 'default' },
  RECURRING: { label: 'Ponavljajuća', color: 'default' },
};

function StatusChip({ map, value, size = 'small' }) {
  const entry = map[value] || { label: value, color: 'default' };
  return <Chip label={entry.label} color={entry.color} size={size} variant="filled" />;
}

export const GatePassStatusChip = (props) => <StatusChip map={GATE_PASS_STATUS} {...props} />;
export const DenialReasonChip = (props) => <StatusChip map={DENIAL_REASON} {...props} />;
export const PersonTypeChip = (props) => <StatusChip map={PERSON_TYPE} {...props} />;
export const RoleChip = (props) => <StatusChip map={ROLE} {...props} />;
export const GatePassTypeChip = (props) => <StatusChip map={GATE_PASS_TYPE} {...props} />;

export const gatePassStatusLabel = (v) => (GATE_PASS_STATUS[v] || {}).label || v;
export const denialReasonLabel = (v) => (DENIAL_REASON[v] || {}).label || v;
export const personTypeLabel = (v) => (PERSON_TYPE[v] || {}).label || v;
export const roleLabel = (v) => (ROLE[v] || {}).label || v;
