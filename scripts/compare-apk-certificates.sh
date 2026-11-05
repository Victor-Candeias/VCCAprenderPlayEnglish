#!/usr/bin/env bash
# PE-008 — Confirm that host and plugin APK certificates are byte-for-byte equal.
# SHA-256 certificate fingerprints are public; no keystore/password is used.
set -euo pipefail

usage() {
  echo "Uso: bash scripts/compare-apk-certificates.sh HOST.apk PLUGIN.apk" >&2
  echo "Ou:  bash scripts/compare-apk-certificates.sh --expected-sha256 FINGERPRINT PLUGIN.apk" >&2
  exit 2
}

[[ "$#" -eq 2 || ( "$#" -eq 3 && "$1" == "--expected-sha256" ) ]] || usage

find_apksigner() {
  if command -v apksigner >/dev/null 2>&1; then
    command -v apksigner
    return 0
  fi
  local sdk candidate found=""
  for sdk in "${ANDROID_HOME:-}" "${ANDROID_SDK_ROOT:-}"; do
    [[ -n "$sdk" ]] || continue
    for candidate in "$sdk"/build-tools/*/apksigner; do
      if [[ -x "$candidate" ]]; then
        found="$candidate"
      fi
    done
  done
  if [[ -n "$found" ]]; then
    printf '%s\n' "$found"
    return 0
  fi
  echo "Erro: apksigner não encontrado. Configura ANDROID_HOME ou PATH." >&2
  return 1
}

APKSIGNER="$(find_apksigner)"

fingerprint() {
  local apk="$1" output fingerprints
  if [[ ! -f "$apk" ]]; then
    echo "Erro: APK não encontrado." >&2
    return 1
  fi
  # Verify cryptographic APK signatures before consulting certificate metadata.
  if ! output="$("$APKSIGNER" verify --print-certs "$apk" 2>&1)"; then
    echo "Erro: APK não tem assinatura válida." >&2
    return 1
  fi
  fingerprints="$(printf '%s\n' "$output" |
    sed -nE 's/^Signer #[0-9]+ certificate SHA-256 digest: ([0-9A-Fa-f]{64})\r?$/\1/p' |
    tr '[:upper:]' '[:lower:]')"
  # The VCCAprender signature policy requires exactly one consistent signer.
  if [[ "$(printf '%s\n' "$fingerprints" | sed '/^$/d' | wc -l | tr -d ' ')" != "1" ]]; then
    echo "Erro: certificado SHA-256 ausente ou múltiplos signatários no APK." >&2
    return 1
  fi
  printf '%s\n' "$fingerprints"
}

if [[ "$1" == "--expected-sha256" ]]; then
  [[ "$#" -eq 3 ]] || usage
  expected="$(printf '%s' "$2" | tr '[:upper:]' '[:lower:]' | tr -d ':')"
  [[ "$expected" =~ ^[0-9a-f]{64}$ ]] || usage
  plugin="$3"
else
  [[ "$#" -eq 2 ]] || usage
  expected="$(fingerprint "$1")" || exit 1
  plugin="$2"
fi

actual="$(fingerprint "$plugin")" || exit 1
if [[ "$actual" != "$expected" ]]; then
  echo "ERRO: certificados diferentes. A VCCAprender rejeitará este plugin." >&2
  echo "Host/esperado SHA-256: $expected" >&2
  echo "Plugin SHA-256:        $actual" >&2
  exit 1
fi
echo "OK: certificado SHA-256 do plugin corresponde ao host."
echo "SHA-256: $actual"
