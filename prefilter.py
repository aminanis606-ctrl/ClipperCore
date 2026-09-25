import re


SEGMENT_RE = re.compile(
    r"\[(\d+(?:\.\d+)?)\s*-\s*"
    r"(\d+(?:\.\d+)?)\]\s*(.*)"
)

SIGNALS = re.compile(
    r"\b("
    r"karena|ternyata|tetapi|tapi|namun|akhirnya|"
    r"berhasil|gagal|masalah|solusi|pengalaman|"
    r"kesalahan|pelajaran|rahasia|pertama|terbesar|"
    r"mengapa|kenapa|bagaimana|uang|gaji|bisnis|"
    r"usaha|hasil|berubah|keputusan|"
    r"menurut saya|pernah|dulu|waktu itu"
    r")\b",
    re.I,
)

BAD = re.compile(
    r"\b("
    r"subscribe|like|comment|follow|jangan lupa|"
    r"terima kasih sudah menonton|website|qr code"
    r")\b",
    re.I,
)


def parse(transcript):
    segments = []

    for line in transcript.splitlines():
        m = SEGMENT_RE.match(line)

        if not m:
            continue

        start = float(m.group(1))
        end = float(m.group(2))
        text = m.group(3).strip()

        if end > start and text:
            segments.append({
                "start": start,
                "end": end,
                "text": text,
                "raw": line,
            })

    return segments


def score(segment):
    text = segment["text"]
    words = len(text.split())
    value = 0

    if 8 <= words <= 60:
        value += 2

    if SIGNALS.search(text):
        value += 4

    if "?" in text:
        value += 2

    if BAD.search(text):
        value -= 8

    return value


def find_candidates(transcript, limit=20):
    segments = parse(transcript)
    candidates = []

    for index, segment in enumerate(segments):
        value = score(segment)

        if value <= 0:
            continue

        start = max(0.0, segment["start"] - 30.0)
        end = segment["end"] + 40.0

        context = [
            s for s in segments
            if s["end"] >= start and s["start"] <= end
        ]

        candidates.append({
            "id": index + 1,
            "anchor_start": segment["start"],
            "anchor_end": segment["end"],
            "context_start": start,
            "context_end": end,
            "score": value,
            "text": "\n".join(
                s["raw"] for s in context
            ),
        })

    candidates.sort(
        key=lambda item: item["score"],
        reverse=True
    )

    return candidates[:limit]
