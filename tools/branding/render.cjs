/* Render editorial layouts around real screenshots. Never redraw or generate application UI. */
const sharp = require('sharp');
const fs = require('node:fs/promises');
const path = require('node:path');
const root = path.resolve(__dirname, '../..');
const brand = path.join(root, 'docs/brand');
const shot = path.join(root, 'docs/screenshots');
const esc = s => String(s).replaceAll('&','&amp;').replaceAll('<','&lt;').replaceAll('"','&quot;');
const text = (x,y,s,size=28,fill='#F6F3EB',weight=400,extra='') => `<text x="${x}" y="${y}" fill="${fill}" font-family="Segoe UI,Arial,sans-serif" font-size="${size}" font-weight="${weight}" ${extra}>${esc(s)}</text>`;
const image = (uri,x,y,w,h,extra='') => `<image href="${uri}" x="${x}" y="${y}" width="${w}" height="${h}" ${extra}/>`;
const data = async file => 'data:image/png;base64,' + (await sharp(file).png().toBuffer()).toString('base64');
function svg(w,h,content) {
  return Buffer.from(`<svg xmlns="http://www.w3.org/2000/svg" width="${w}" height="${h}" viewBox="0 0 ${w} ${h}">
  <defs><radialGradient id="ambient"><stop stop-color="#2D315C"/><stop offset="1" stop-color="#101321"/></radialGradient>
  <linearGradient id="line"><stop stop-color="#EBC17E"/><stop offset="1" stop-color="#A8AFF4"/></linearGradient>
  <filter id="shadow" x="-30%" y="-30%" width="160%" height="180%"><feDropShadow dx="0" dy="24" stdDeviation="28" flood-color="#000" flood-opacity=".45"/></filter>
  <clipPath id="photo"><rect x="750" y="200" width="982" height="736" rx="20"/></clipPath>
  </defs><rect width="${w}" height="${h}" fill="#101321"/>${content}</svg>`);
}
async function main() {
  const logo=await data(path.join(brand,'milo-icon.png'));
  const photo=await data(path.join(shot,'haier-user-photo.webp'));
  const channels=await data(path.join(shot,'update-channels.png'));
  const online=await data(path.join(shot,'online-rc-menu.png'));
  for(const lang of ['ru','en']) {
    const ru=lang==='ru';
    const copy=ru?['Ваш ТВ.','Ничего','лишнего.']:['Your TV.','Less noise.','More home.'];
    const content=`<ellipse cx="1330" cy="550" rx="900" ry="660" fill="url(#ambient)"/>
      <path d="M0 1038 H1800" stroke="url(#line)" stroke-width="4"/>
      ${image(logo,68,58,86,86)}${text(176,115,'Milo Home',46,'#F6F3EB',600)}
      ${text(78,215,ru?'ANDROID TV  /  ДОМАШНИЙ ЭКРАН':'ANDROID TV  /  MADE PERSONAL',18,'#A8AFF4',600,'letter-spacing="3"')}
      ${copy.map((s,i)=>text(70,350+i*126,s,104,'#F6F3EB',600,'letter-spacing="-4"')).join('')}
      <rect x="78" y="690" width="70" height="4" rx="2" fill="#EBC17E"/>
      ${text(78,760,ru?'Лёгкий лаунчер. Удобный пульт.':'A lightweight launcher. Made for your remote.',27,'#C8CAD8')}
      ${text(78,805,ru?'Обновления — под вашим контролем.':'Updates on your terms.',27,'#C8CAD8')}
      ${text(78,952,ru?'На базе CleanYourTV от Gathin23':'Built on CleanYourTV by Gathin23',20,'#959BAF')}
      <rect x="738" y="188" width="1006" height="760" rx="30" fill="#090B12" stroke="#535672" stroke-width="2" filter="url(#shadow)"/>
      ${image(photo,750,200,982,736,'clip-path="url(#photo)" preserveAspectRatio="xMidYMid slice"')}
      ${text(760,998,ru?'Реальный кадр · Haier 32 Smart TV BX':'Real TV photo · Haier 32 Smart TV BX',20,'#A9AEC0')}`;
    await sharp(svg(1800,1040,content)).webp({quality:92}).toFile(path.join(brand,`hero-${lang}.webp`));
    const gallery=`${text(76,108,ru?'Обновления — по вашему решению.':'You decide when to update.',64,'#F6F3EB',600)}
      ${text(78,168,ru?'Stable или RC. Сначала подтверждение, затем установка.':'Stable or RC. Confirm first, then install.',29,'#B9BED2')}
      ${text(78,260,ru?'01 / ВЫБОР ВЕРСИИ · ЛОКАЛЬНОЕ ДЕМО':'01 / CHOOSE A VERSION · LOCAL DEMO',18,'#A8AFF4',600,'letter-spacing="1.5"')}
      ${text(928,260,ru?'02 / НАСТОЯЩИЙ RC ИЗ GITHUB':'02 / REAL RC FROM GITHUB',18,'#EBC17E',600,'letter-spacing="1.5"')}
      <rect x="65" y="283" width="830" height="478" rx="24" fill="#090B12" stroke="#3C4059"/>
      <rect x="915" y="283" width="830" height="478" rx="24" fill="#090B12" stroke="#3C4059"/>
      ${image(channels,80,298,800,450)}${image(online,930,298,800,450)}
      ${text(78,850,ru?'Проверка SHA-256 и подписи APK':'SHA-256 and APK signature verification',28,'#F6F3EB',600)}
      ${text(928,850,ru?'Штатный установщик Android':'The standard Android installer',28,'#F6F3EB',600)}
      ${text(78,932,ru?'Реальные снимки Android 11 эмулятора. Испытания на Haier — отдельный этап.':'Actual Android 11 emulator captures. Haier installation is a separate test.',22,'#959BAF')}`;
    await sharp(svg(1800,1000,gallery)).webp({quality:93}).toFile(path.join(brand,`updates-${lang}.webp`));
  }
  const banner=`${image(logo,12,22,136,136)}${text(155,86,'Milo',34,'#F6F3EB',600)}${text(155,126,'Home',34,'#F6F3EB',600)}<rect x="161" y="143" width="52" height="3" rx="1.5" fill="#EBC17E"/>`;
  await sharp(svg(320,180,banner)).png().toFile(path.join(root,'launcher/app/src/main/res/drawable-nodpi/banner.png'));
  const social=`<ellipse cx="1000" cy="340" rx="650" ry="500" fill="url(#ambient)"/>${image(logo,60,56,106,106)}
    ${text(190,127,'Milo Home',62,'#F6F3EB',600)}
    ${text(64,290,'Ваш ТВ.',66,'#F6F3EB',600)}${text(64,372,'Ничего лишнего.',66,'#F6F3EB',600)}
    ${text(68,486,'Лаунчер для Android TV',25,'#B9BED2')}${text(68,562,'На базе CleanYourTV от Gathin23',18,'#959BAF')}
    <rect x="660" y="92" width="570" height="450" rx="20" fill="#090B12" stroke="#535672"/>
    ${image(photo,674,106,542,422,'preserveAspectRatio="xMidYMid meet"')}`;
  await sharp(svg(1280,640,social)).png({palette:true,colours:256}).toFile(path.join(brand,'social-preview.png'));
  console.log('Exported bilingual hero/gallery, Android TV banner and social preview.');
}
main().catch(error=>{console.error(error);process.exitCode=1;});
