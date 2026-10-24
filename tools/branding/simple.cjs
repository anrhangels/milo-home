/* Minimal editorial frames. Application screenshots are never redrawn. */
const sharp=require('sharp'), fs=require('node:fs/promises'),path=require('node:path');
const root=path.resolve(__dirname,'../..'),brand=path.join(root,'docs/brand');
const txt=(x,y,s,n=28,c='#f4f3f8')=>`<text x="${x}" y="${y}" font-family="Segoe UI,Arial,sans-serif" font-size="${n}" fill="${c}">${s}</text>`;
const pic=async(f,x,y,w,h)=>`<image href="data:image/png;base64,${(await fs.readFile(f)).toString('base64')}" x="${x}" y="${y}" width="${w}" height="${h}"/>`;
const frame=(w,h,body)=>Buffer.from(`<svg xmlns="http://www.w3.org/2000/svg" width="${w}" height="${h}"><rect width="${w}" height="${h}" rx="22" fill="#13151d"/>${body}</svg>`);
(async()=>{
 const mascot=await pic(path.join(brand,'milo-mascot.png'),36,24,94,94);
 const shot=path.join(root,'docs/screenshots/haier-media-fresh.png');
 for(const lang of ['en','ru']){
 const ru=lang==='ru';
 const body=mascot+txt(142,84,'Milo Home',34)+txt(46,190,ru?'Ваш ТВ. Ничего лишнего.':'Your TV. A little less clutter.',48)+txt(48,240,ru?'Любимые приложения. Привычный пульт.':'Your favourite apps. Your familiar remote.',23,'#b8bac8')+`<rect x="42" y="282" width="1116" height="634" rx="14" fill="#08090d"/>`+await pic(shot,50,290,1100,618.75);
 await sharp(frame(1200,950,body)).webp({quality:91}).toFile(path.join(brand,`milo-simple-${lang}.webp`));
 }
 let gallery='';for(const [i,name] of ['haier-home-fresh.png','haier-media-fresh.png'].entries()){const x=22+i*590;gallery+=`<rect x="${x}" y="22" width="566" height="323" rx="10" fill="#08090d"/>`+await pic(path.join(root,'docs/screenshots',name),x+6,28,554,311.625);}
 await sharp(frame(1200,367,gallery)).webp({quality:92}).toFile(path.join(brand,'milo-interface.webp'));
 const steps=[['1','Download','Choose Stable.','Скачать','Выберите Stable.'],['2','Install','Follow your computer guide.','Установить','Откройте инструкцию для ПК.'],['3','Make it home','Try the remote. Then choose Milo.','Выбрать Milo','Проверьте пульт. Выберите Milo.']];
 for(const lang of ['en','ru']){let body='';for(const [i,s]of steps.entries()){let x=30+i*390;body+=`<rect x="${x}" y="30" width="360" height="160" rx="14" fill="#20232e"/>`+txt(x+20,73,s[0],26,'#c7baff')+txt(x+20,116,s[lang==='ru'?3:1],27)+txt(x+20,157,s[lang==='ru'?4:2],17,'#b8bac8');}await sharp(frame(1200,220,body)).webp().toFile(path.join(brand,`install-steps-${lang}.webp`));}
 console.log('Exported minimal covers, gallery and bilingual instructional diagrams.');
})().catch(e=>{console.error(e);process.exitCode=1});
