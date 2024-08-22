const fs = require('fs');

const { getBizCount } = require("./utils");

function padTo2Digits(num) {
  return num.toString().padStart(2, '0');
}

function formatDate(date) {
  return (
    [
      date.getFullYear(),
      padTo2Digits(date.getMonth() + 1),
      padTo2Digits(date.getDate()),
    ].join('-')
  );
}

let reportT = '{"text":"📈 Ingestion BizView 2023 :\\n", "months":[\
  {"month":"January","INGESTED":0,"TODO":0},\
  {"month":"February","INGESTED":0,"TODO":0},\
  {"month":"March","INGESTED":0,"TODO":0},\
  {"month":"April","INGESTED":0,"TODO":0},\
  {"month":"May","INGESTED":0,"TODO":0},\
  {"month":"June","INGESTED":0,"TODO":0},\
  {"month":"July","INGESTED":0,"TODO":0},\
  {"month":"August","INGESTED":0,"TODO":0},\
  {"month":"September","INGESTED":0,"TODO":0},\
  {"month":"October","INGESTED":0,"TODO":0},\
  {"month":"November","INGESTED":0,"TODO":0},\
  {"month":"December","INGESTED":0,"TODO":0}]\
}'

report = JSON.parse(reportT);
// console.log(report);

// const months4Year = [7,6,5,4,3,2,1]; // giu'24 to gen'24
const monthSelected = process.env.MONTH_SELECTED || "7";
const months4Year = [monthSelected];

console.log(`MONTH_SELECTED ${monthSelected}`)

// [
//   { eventStatusCount: 29, eventStatus: 'INGESTED' },
//   { eventStatusCount: 34498723, eventStatus: 'DONE' }
// ]
// Start function
const start = async function (month_, a, b) {
  const resBiz = await getBizCount(a + "T00:00:00", b + "T23:59:59");
  // console.log(`📈 _Report ingestion month *${month[month_]}*\n`);
  report.months[+(month_-1)].INGESTED = resBiz.resources[0].eventStatus == "INGESTED" ? resBiz.resources[0].eventStatusCount : resBiz.resources[1].eventStatusCount ;
  report.months[+(month_-1)].TODO = resBiz.resources[0].eventStatus == "DONE" ? resBiz.resources[0].eventStatusCount : resBiz.resources[1].eventStatusCount ;
  // console.log(resBiz.resources);
  // console.log("End\t>> ", new Date().toLocaleString());
};


const endReport = async function() {
  await_list = [];
  for (let monthIdx of months4Year) {
    const from = `2023-0${monthIdx}-01T00:00:00`;
    const to = `2023-0${monthIdx}-30T23:59:59`;
    dateFrom = formatDate(new Date(from));
    dateTo = formatDate(new Date(to))
    await_list.push(start(monthIdx,dateFrom,dateTo));
  }
  await Promise.all(await_list);
  // console.log(report);
  for (let m of report.months) {
    // console.log(m);
    report.text +=`${m.month.toString().padEnd(12,' ')} - 🟢 INGESTED:\`${m.INGESTED.toLocaleString('it-IT').padEnd(12,' ')}\` 🔴 TODO:\`${m.TODO.toLocaleString('it-IT')}\`\n`
  }
  delete report["months"]

  console.log(report);
  fs.writeFileSync('report.json', JSON.stringify(report));
}

const r = endReport()

