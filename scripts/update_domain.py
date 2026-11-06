#!/usr/bin/env python3
"""Legge l'ultimo dominio dal canale Telegram ufficiale e aggiorna il plugin.

Eseguilo manualmente con:  python3 scripts/update_domain.py
La GitHub Action lo esegue ogni 6 ore.
"""

from __future__ import annotations

import re
import sys
import urllib.request
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
SOURCE = ROOT / "StreamingCommunity/src/main/kotlin/it/dogior/hadEnough/StreamingCommunity.kt"
BUILD_FILE = ROOT / "StreamingCommunity/build.gradle.kts"
README = ROOT / "README.md"

TELEGRAM_URL = "https://t.me/s/Streaming_community_sito"
USER_AGENT = (
    "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 "
    "(KHTML, like Gecko) Chrome/124.0 Mobile Safari/537.36"
)
DOMAIN_RE = re.compile(
    r"\b((?:streamingcommunity[a-z0-9-]*|streamingunity)\.[a-z]{2,})\b",
    re.IGNORECASE,
)


def fetch(url: str) -> str:
    request = urllib.request.Request(url, headers={"User-Agent": USER_AGENT})
    with urllib.request.urlopen(request, timeout=30) as response:
        return response.read().decode("utf-8", "replace")


def latest_domain_candidates(html: str) -> list[str]:
    """Prende i domini che seguono l'ultimo blocco 'Nuovo:' del canale."""
    marker = html.lower().rfind("nuovo:")
    tail = html[marker:] if marker >= 0 else html
    found = [match.group(1).lower() for match in DOMAIN_RE.finditer(tail)]
    return list(dict.fromkeys(found))


def is_working(domain: str) -> bool:
    try:
        page = fetch(f"https://{domain}/")
        return "data-page" in page or "streamingcommunity" in page.lower()
    except Exception as error:  # noqa: BLE001 - il dominio può essere offline
        print(f"  {domain}: non raggiungibile ({error})")
        return False


def main() -> int:
    print(f"Leggo {TELEGRAM_URL} ...")
    try:
        html = fetch(TELEGRAM_URL)
    except Exception as error:  # noqa: BLE001
        print(f"Errore nel leggere Telegram: {error}", file=sys.stderr)
        return 1

    candidates = latest_domain_candidates(html)
    if not candidates:
        print("Nessun dominio trovato nel messaggio Telegram.", file=sys.stderr)
        return 1

    print("Domini trovati: " + ", ".join(candidates))
    new_domain = next((domain for domain in candidates if is_working(domain)), None)
    if new_domain is None:
        print("Nessun dominio del canale risponde: non aggiorno nulla.", file=sys.stderr)
        return 1

    source_text = SOURCE.read_text(encoding="utf-8")
    match = re.search(r'DEFAULT_BASE_URL = "https://([^/"]+)/"', source_text)
    if not match:
        print("DEFAULT_BASE_URL non trovato nel provider.", file=sys.stderr)
        return 1

    old_domain = match.group(1).lower()
    print(f"Dominio attuale: {old_domain}")
    print(f"Dominio Telegram: {new_domain}")

    if old_domain == new_domain:
        print("Nessun aggiornamento necessario.")
        return 0

    build_text = BUILD_FILE.read_text(encoding="utf-8")
    version_match = re.search(r"^version = (\d+)$", build_text, re.MULTILINE)
    if not version_match:
        print("Versione del plugin non trovata.", file=sys.stderr)
        return 1

    new_version = int(version_match.group(1)) + 1
    build_text = (
        build_text[: version_match.start(1)]
        + str(new_version)
        + build_text[version_match.end(1) :]
    )
    source_text = re.sub(re.escape(old_domain), new_domain, source_text, flags=re.IGNORECASE)
    build_text = re.sub(re.escape(old_domain), new_domain, build_text, flags=re.IGNORECASE)

    SOURCE.write_text(source_text, encoding="utf-8")
    BUILD_FILE.write_text(build_text, encoding="utf-8")

    if README.exists():
        readme_text = README.read_text(encoding="utf-8")
        README.write_text(
            re.sub(re.escape(old_domain), new_domain, readme_text, flags=re.IGNORECASE),
            encoding="utf-8",
        )

    print(f"Aggiornato a {new_domain} (versione plugin {new_version}).")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
