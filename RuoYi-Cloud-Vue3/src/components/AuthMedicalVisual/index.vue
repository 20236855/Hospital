<template>
  <section class="medical-visual" aria-hidden="true">
    <div class="visual-copy">
      <span class="visual-kicker">Smart Hospital Care</span>
      <strong>智慧诊疗</strong>
      <p>温暖守护每一次就医</p>
    </div>

    <div class="orbit orbit-one">
      <i></i>
      <i></i>
      <i></i>
    </div>
    <div class="orbit orbit-two">
      <i></i>
      <i></i>
      <i></i>
      <i></i>
    </div>

    <svg class="medical-art" viewBox="0 0 640 560" role="img" aria-label="医疗主题插画">
      <defs>
        <linearGradient id="cloudGradient" x1="0" x2="1" y1="0" y2="1">
          <stop offset="0" stop-color="#eef6fc" />
          <stop offset="1" stop-color="#d6ebfb" />
        </linearGradient>
        <linearGradient id="mintGradient" x1="0" x2="1" y1="0" y2="1">
          <stop offset="0" stop-color="#a8d4f7" />
          <stop offset="1" stop-color="#7eb8f0" />
        </linearGradient>
        <linearGradient id="blueGradient" x1="0" x2="1" y1="0" y2="1">
          <stop offset="0" stop-color="#c9e4f9" />
          <stop offset="1" stop-color="#8fc5f0" />
        </linearGradient>
        <filter id="softShadow" x="-20%" y="-20%" width="140%" height="140%">
          <feDropShadow dx="0" dy="18" stdDeviation="18" flood-color="#7eb8f0" flood-opacity=".2" />
        </filter>
      </defs>

      <g class="wave-lines" fill="none" stroke="#c9e4f9" stroke-linecap="round" stroke-width="3">
        <path d="M34 380 C 148 332, 230 430, 330 374 S 522 328, 608 390" />
        <path d="M60 418 C 176 370, 238 448, 342 406 S 510 380, 586 432" />
        <path d="M80 456 C 188 422, 250 470, 360 440 S 502 420, 566 466" />
      </g>

      <g class="cloud cloud-a" fill="url(#cloudGradient)">
        <path d="M83 150c5-29 30-50 60-50 23 0 43 13 53 32 8-4 18-6 28-6 35 0 63 28 63 62H92c-30 0-54-24-54-54s24-54 54-54c2 0 4 0 6 1-7 8-12 18-15 29Z" />
      </g>
      <g class="cloud cloud-b" fill="url(#cloudGradient)">
        <path d="M410 96c6-22 26-38 50-38 19 0 36 10 45 26 7-3 14-4 22-4 30 0 54 24 54 54H396c-24 0-44-20-44-44s20-44 44-44c8 0 15 2 21 5-3 5-5 10-7 15Z" />
      </g>

      <g class="floating icon-card form-card" filter="url(#softShadow)">
        <rect x="82" y="225" width="112" height="130" rx="24" fill="#eef6fc" />
        <rect x="112" y="248" width="52" height="14" rx="7" fill="#a8d4f7" />
        <rect x="108" y="282" width="68" height="10" rx="5" fill="#d6ebfb" />
        <rect x="108" y="306" width="58" height="10" rx="5" fill="#d6ebfb" />
        <path d="M126 334h28" stroke="#7eb8f0" stroke-width="12" stroke-linecap="round" />
      </g>

      <g class="floating icon-card med-kit" filter="url(#softShadow)">
        <rect x="438" y="180" width="120" height="96" rx="22" fill="#eef6fc" />
        <path d="M472 180v-22c0-8 7-15 15-15h22c8 0 15 7 15 15v22" fill="none" stroke="#8fc5f0" stroke-width="10" stroke-linecap="round" />
        <path d="M498 206v46M475 229h46" stroke="#f0a5b8" stroke-width="12" stroke-linecap="round" />
      </g>

      <g class="floating stethoscope" fill="none" stroke="#7eb8f0" stroke-linecap="round" stroke-width="10">
        <path d="M402 338v48c0 38-28 70-64 70s-64-32-64-70v-28" />
        <path d="M248 338v38c0 24-18 44-40 44s-40-20-40-44v-38" />
        <circle cx="402" cy="332" r="14" fill="#eef6fc" />
      </g>

      <g class="heart-pulse" fill="none" stroke="#ef8fa9" stroke-linecap="round" stroke-linejoin="round" stroke-width="8">
        <path d="M306 140c-22-25-62-9-62 26 0 35 62 70 62 70s62-35 62-70c0-35-40-51-62-26Z" fill="#f8f4f6" />
        <path class="pulse-line" d="M252 182h35l12-24 20 48 14-28h39" />
      </g>

      <g class="pill pill-a">
        <rect x="210" y="130" width="86" height="34" rx="17" fill="#eef6fc" transform="rotate(-18 253 147)" />
        <path d="M253 130v34" stroke="#7eb8f0" stroke-width="4" transform="rotate(-18 253 147)" />
      </g>
      <g class="pill pill-b">
        <rect x="484" y="360" width="76" height="30" rx="15" fill="#d6ebfb" transform="rotate(23 522 375)" />
        <path d="M522 360v30" stroke="#efb2c1" stroke-width="4" transform="rotate(23 522 375)" />
      </g>

      <g class="hospital" filter="url(#softShadow)">
        <path d="M146 494h388c18 0 32-14 32-32v-24H114v24c0 18 14 32 32 32Z" fill="#d6ebfb" />
        <rect x="174" y="284" width="292" height="210" rx="26" fill="#eef6fc" />
        <rect x="238" y="224" width="164" height="270" rx="28" fill="#eef6fc" />
        <rect x="274" y="254" width="92" height="68" rx="18" fill="url(#blueGradient)" />
        <path d="M320 268v38M301 287h38" stroke="#eef6fc" stroke-width="12" stroke-linecap="round" />
        <g fill="#a8d4f7">
          <rect x="208" y="326" width="42" height="38" rx="10" />
          <rect x="208" y="386" width="42" height="38" rx="10" />
          <rect x="410" y="326" width="42" height="38" rx="10" />
          <rect x="410" y="386" width="42" height="38" rx="10" />
        </g>
        <rect x="290" y="382" width="80" height="112" rx="18" fill="url(#mintGradient)" />
        <path d="M330 382v112" stroke="#d6ebfb" stroke-width="5" />
      </g>

      <g class="plant plant-left">
        <path d="M118 494c0-46 18-86 56-118" fill="none" stroke="#8bc9ae" stroke-width="8" stroke-linecap="round" />
        <path d="M138 444c-28-6-44-24-48-52 30 1 52 18 60 48Z" fill="#b9e1cd" />
        <path d="M157 405c5-27 24-45 56-54-2 33-21 56-52 66Z" fill="#9bd5bc" />
      </g>
      <g class="plant plant-right">
        <path d="M550 494c-4-42-22-74-54-98" fill="none" stroke="#8bc9ae" stroke-width="8" stroke-linecap="round" />
        <path d="M522 434c24-10 42-27 52-52-30-2-55 13-66 42Z" fill="#b9e1cd" />
      </g>

      <g class="tiny-crosses" fill="none" stroke="#8fc5f0" stroke-linecap="round" stroke-width="5">
        <path d="M132 92v24M120 104h24" />
        <path d="M516 150v22M505 161h22" />
        <path d="M76 405v18M67 414h18" />
        <path d="M564 448v18M555 457h18" />
      </g>
    </svg>
  </section>
</template>

<style lang="scss" scoped>
.medical-visual {
  position: relative;
  min-height: 620px;
  overflow: hidden;
  border-radius: 32px 0 0 32px;
  background:
    linear-gradient(135deg, #eef6fc, #d0e7fb),
    repeating-linear-gradient(110deg, rgba(127, 184, 240, .12) 0 1px, transparent 1px 34px);
}


.visual-copy {
  position: absolute;
  top: 44px;
  left: 52px;
  z-index: 3;
  color: #5f7580;

  .visual-kicker {
    display: block;
    margin-bottom: 12px;
    color: #6da0d0;

    font-size: 13px;
    font-weight: 700;
    letter-spacing: .08em;
    text-transform: uppercase;
  }

  strong {
    display: block;
    color: #4f7380;
    font-size: 34px;
    line-height: 1.2;
    letter-spacing: 0;
  }

  p {
    margin: 10px 0 0;
    color: #8e9fa8;
    font-size: 16px;
  }
}

.medical-art {
  position: absolute;
  inset: auto 0 0;
  width: 100%;
  height: 88%;
}

.orbit {
  position: absolute;
  z-index: 2;
  border: 1px solid rgba(126, 184, 240, .4);

  border-radius: 50%;
  animation: orbitFloat 16s linear infinite;

  i {
    position: absolute;
    width: 18px;
    height: 18px;

    &::before,
    &::after {
      position: absolute;
      content: "";
      border-radius: 8px;
      background: rgba(126, 184, 240, .6);
    }

    &::before {
      top: 8px;
      left: 0;
      width: 18px;
      height: 4px;
    }

    &::after {
      top: 0;
      left: 7px;
      width: 4px;
      height: 18px;
    }
  }
}

.orbit-one {
  right: 88px;
  top: 88px;
  width: 214px;
  height: 214px;

  i:nth-child(1) { top: 5px; left: 64px; }
  i:nth-child(2) { right: 10px; top: 120px; animation-delay: -2s; }
  i:nth-child(3) { left: 28px; bottom: 24px; animation-delay: -4s; }
}

.orbit-two {
  left: 80px;
  bottom: 118px;
  width: 292px;
  height: 292px;
  animation-duration: 22s;
  animation-direction: reverse;

  i:nth-child(1) { top: 36px; right: 42px; }
  i:nth-child(2) { left: 0; top: 138px; animation-delay: -1s; }
  i:nth-child(3) { right: 36px; bottom: 24px; animation-delay: -3s; }
  i:nth-child(4) { left: 82px; bottom: 0; animation-delay: -5s; }
}

.wave-lines {
  animation: waveMove 9s ease-in-out infinite;
}

.cloud {
  opacity: .88;
  animation: cloudDrift 9s ease-in-out infinite;
}

.cloud-b {
  animation-delay: -3s;
}

.floating,
.pill,
.plant {
  animation: floatSoft 5.8s ease-in-out infinite;
}

.form-card { animation-delay: -.8s; }
.med-kit { animation-delay: -1.4s; }
.stethoscope { animation-delay: -2s; }
.pill-a { animation-delay: -2.8s; }
.pill-b { animation-delay: -1.8s; }
.plant-left { animation-delay: -1.2s; }
.plant-right { animation-delay: -2.4s; }

.heart-pulse {
  animation: heartbeat 3.4s ease-in-out infinite;
  transform-origin: 306px 214px;
}

.pulse-line {
  stroke-dasharray: 190;
  stroke-dashoffset: 190;
  animation: pulseDraw 3.4s ease-in-out infinite;
}

.tiny-crosses {
  animation: twinkle 4.8s ease-in-out infinite;
}

@keyframes orbitFloat {
  to { transform: rotate(360deg); }
}

@keyframes waveMove {
  0%, 100% { transform: translateX(-8px) translateY(0); opacity: .48; }
  50% { transform: translateX(12px) translateY(-8px); opacity: .76; }
}

@keyframes cloudDrift {
  0%, 100% { transform: translateX(0); }
  50% { transform: translateX(18px); }
}

@keyframes floatSoft {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-12px); }
}

@keyframes heartbeat {
  0%, 100% { transform: scale(1); }
  45% { transform: scale(1.035); }
  60% { transform: scale(.99); }
}

@keyframes pulseDraw {
  0% { stroke-dashoffset: 190; opacity: .2; }
  45%, 75% { stroke-dashoffset: 0; opacity: 1; }
  100% { stroke-dashoffset: -190; opacity: .25; }
}

@keyframes twinkle {
  0%, 100% { opacity: .38; }
  50% { opacity: .88; }
}

@media (max-width: 1024px) {
  .medical-visual {
    min-height: 280px;
    border-radius: 28px 28px 0 0;
  }

  .visual-copy {
    top: 28px;
    left: 32px;

    strong {
      font-size: 28px;
    }
  }

  .medical-art {
    right: -10%;
    width: 86%;
    height: 94%;
  }

  .orbit-two {
    display: none;
  }
}

@media (max-width: 640px) {
  .medical-visual {
    min-height: 190px;
  }

  .visual-copy {
    top: 22px;
    left: 24px;

    .visual-kicker {
      font-size: 11px;
    }

    strong {
      font-size: 23px;
    }

    p {
      font-size: 13px;
    }
  }

  .medical-art {
    right: -24%;
    width: 92%;
  }
}
</style>
