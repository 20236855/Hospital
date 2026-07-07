const fs = require('fs');
const path = require('path');

const viewDir = path.join(__dirname, 'src', 'views');
const files = ['Register.vue', 'Profile.vue', 'Record.vue', 'MyAppointments.vue', 'PatientComplete.vue', 'HealthNews.vue'];

const colorMap = {
  '#68c7a9': '#5b9bd5',
  '#89dbc1': '#90bae8',
  '#68a895': '#5a8ec4',
  '#4a9f87': '#3b75b5',
  '#48c9b0': '#4398d6',
  '#77b9a8': '#6fa5d8',
};

for (const file of files) {
  const fp = path.join(viewDir, file);
  if (!fs.existsSync(fp)) { console.log(`SKIP: ${file}`); continue; }
  let content = fs.readFileSync(fp, 'utf8');
  let changed = false;
  for (const [oldColor, newColor] of Object.entries(colorMap)) {
    if (content.includes(oldColor)) {
      content = content.split(oldColor).join(newColor);
      changed = true;
    }
  }
  if (changed) {
    fs.writeFileSync(fp, content, 'utf8');
    console.log(`DONE: ${file}`);
  } else {
    console.log(`NOCHANGE: ${file}`);
  }
}
console.log('All done!');
