/**
 * /js/home.js — 今日新增一般/急件以「本日 receivedAt」直接計算（含分頁）
 * - KPI「新增一般件」「新增急件」以 returnAt - receivedAt 的日曆天數（含週末）分類：
 *   < 7 天 => 急件；>= 7 天 => 一般件；缺任一日期 => 排除於分類，但仍計入「新增訂單」總數
 */

(() => {
  // ---------- Base URL 與小工具 ----------
  const pickBaseUrl = () => {
    if (typeof window !== 'undefined' && window.API_BASE_URL) return String(window.API_BASE_URL);
    const o = (typeof window !== 'undefined' && window.location && window.location.origin) ? window.location.origin : '';
    if (!/^https?:\/\//i.test(o)) return 'http://localhost:8080';
    return o;
  };
  const BASE_URL = pickBaseUrl();

  const $ = (id) => document.getElementById(id);
  const p2 = (n) => (n < 10 ? "0" + n : "" + n);

  const toIsoLocal = (d) =>
    `${d.getFullYear()}-${p2(d.getMonth() + 1)}-${p2(d.getDate())}T${p2(d.getHours())}:${p2(d.getMinutes())}:${p2(d.getSeconds())}`;

  const fmtDate = (s) => {
    if (!s) return "-";
    try { return new Date(s).toISOString().slice(0, 10); } catch { return s; }
  };

  const todayRange = () => {
    const start = new Date(); start.setHours(0, 0, 0, 0);
    const end = new Date(); end.setHours(23, 59, 59, 999);
    return { start, end };
  };
  const lastNDaysRange = (n) => {
    const end = new Date(); end.setHours(23, 59, 59, 999);
    const start = new Date(); start.setDate(end.getDate() - (n - 1)); start.setHours(0, 0, 0, 0);
    return { start, end };
  };

  async function fetchJSONAbs(url, init) {
    const resp = await fetch(url, { credentials: "include", ...init });
    if (!resp.ok) throw new Error(`[${resp.status}] ${url}`);
    const ct = resp.headers.get("content-type") || "";
    return ct.includes("application/json") ? resp.json() : resp.text();
  }

  // ---------- 1) 目前使用者（先 localStorage，後 API） ----------
  function readUserFromStorage() {
    try {
      const uObj = JSON.parse(localStorage.getItem('user') || '{}');
      const username = (uObj.username || uObj.name || localStorage.getItem('loginUser') || localStorage.getItem('loginEmpNo') || '').toString().trim();
      const role = (uObj.role || localStorage.getItem('loginRole') || '').toString().trim().toLowerCase();
      return { username, role };
    } catch {
      return { username: '', role: '' };
    }
  }

  async function loadMe() {
    const { username, role } = readUserFromStorage();
    if (username) {
      $("currentUser").textContent = role ? `${username}｜${role}` : username;
    } else {
      $("currentUser").textContent = '未登入';
    }

    try {
      const empNo = localStorage.getItem("loginEmpNo");
      if (!empNo) return;

      let me = null;
      const tryUrls = [
        `${BASE_URL}/api/members/${encodeURIComponent(empNo)}`,
        `${BASE_URL}/api/members/empNo/${encodeURIComponent(empNo)}`,
      ];
      for (const u of tryUrls) {
        try { me = await fetchJSONAbs(u); if (me) break; } catch (_) { }
      }
      if (!me) return;

      const name = me.name || me.username || username || empNo;
      const r = (me.role || role || '').toString().toLowerCase();
      $("currentUser").textContent = r ? `${name}｜${r}` : name;
    } catch (e) {
      console.warn("loadMe API:", e.message);
    }
  }

  // ---------- 2) KPI（今日新增訂單/一般件/急件） ----------
  function daysBetween(startStr, endStr) {
    if (!startStr || !endStr) return null;
    const s = new Date(startStr);
    const e = new Date(endStr);
    if (isNaN(s.getTime()) || isNaN(e.getTime())) return null;
    s.setHours(0, 0, 0, 0);
    e.setHours(0, 0, 0, 0);
    const msPerDay = 24 * 60 * 60 * 1000;
    return Math.floor((e - s) / msPerDay);
  }

  async function fetchAllCasesToday() {
    const { start, end } = todayRange();
    const base = `${BASE_URL}/api/cases`;
    const size = 200;

    const buildUrl = (page) => {
      const qs = new URLSearchParams({
        receivedAtStart: toIsoLocal(start),
        receivedAtEnd: toIsoLocal(end),
        sort: "receivedAt,desc",
        page: String(page),
        size: String(size)
      }).toString();
      return `${base}?${qs}`;
    };

    const first = await fetchJSONAbs(buildUrl(0));
    if (Array.isArray(first)) return first;

    const content = Array.isArray(first?.content) ? first.content : [];
    const total = typeof first?.totalElements === 'number' ? first.totalElements : null;

    if (total === null) {
      let all = [...content];
      let page = 1;
      while (content.length === size && page < 20) {
        const data = await fetchJSONAbs(buildUrl(page));
        const chunk = Array.isArray(data?.content) ? data.content : (Array.isArray(data) ? data : []);
        if (!Array.isArray(chunk) || chunk.length === 0) break;
        all = all.concat(chunk);
        if (chunk.length < size) break;
        page++;
      }
      return all;
    }

    const totalPages = Math.ceil(total / size);
    let all = [...content];
    for (let p = 1; p < totalPages; p++) {
      const data = await fetchJSONAbs(buildUrl(p));
      const chunk = Array.isArray(data?.content) ? data.content : [];
      all = all.concat(chunk);
      if (chunk.length < size) break;
    }
    return all;
  }

  function normalizeCase(x) {
    if (!x || typeof x !== "object") return null;
    return {
      receivedAt: x.receivedAt ?? x.received_at ?? "",
      returnAt: x.returnAt ?? x.return_at ?? "",
    };
  }

  async function loadKPI() {
    try {
      const raw = await fetchAllCasesToday();
      const list = (Array.isArray(raw) ? raw : []).map(normalizeCase).filter(Boolean);

      const totalToday = list.length;

      let normal = 0, rush = 0;
      for (const r of list) {
        const diff = daysBetween(r.receivedAt, r.returnAt);
        if (diff === null) continue;
        if (diff < 7) rush++; else normal++;
      }

      const totalEl = $("kpiOrdersToday");
      const normalEl = $("kpiNormalCount");
      const rushEl = $("kpiRushCount");
      if (totalEl) totalEl.textContent = totalToday;
      if (normalEl) normalEl.textContent = normal;
      if (rushEl) rushEl.textContent = rush;
    } catch (e) {
      console.warn("loadKPI:", e.message);
      const totalEl = $("kpiOrdersToday");
      const normalEl = $("kpiNormalCount");
      const rushEl = $("kpiRushCount");
      if (totalEl) totalEl.textContent = "--";
      if (normalEl) normalEl.textContent = "--";
      if (rushEl) rushEl.textContent = "--";
    }
  }

  // ---------- 3) 診所營收佔比（甜甜圈 + 右側清單分頁） ----------
  let clinicRevenueChart = null;
  const fmtTWD = new Intl.NumberFormat('zh-TW', { style: 'currency', currency: 'TWD', maximumFractionDigits: 0 });

  // 右側清單分頁狀態（只影響 legend，不影響圖表）
  let legendData = [];      // [{ clinicName, totalAmount }]
  let legendValues = [];    // [number]
  let legendColors = [];    // [color string]
  let legendTotal = 1;      // 總金額（避免除以 0）
  let legendPageIndex = 0;  // 0-based
  let legendPageSize = 10;  // 依視窗寬度響應計算
  let legendPagerBound = false;

  function computeLegendPageSize() {
    return 5; // 固定 5 筆/頁
  }

  function updateLegendPagerUI() {
    const prevBtn = $("legendPrev");
    const nextBtn = $("legendNext");
    const info = $("legendPageInfo");
    const totalPages = Math.max(1, Math.ceil(legendData.length / legendPageSize));

    if (info) info.textContent = `第 ${Math.min(legendPageIndex + 1, totalPages)} / ${totalPages} 頁`;
    if (prevBtn) {
      prevBtn.disabled = legendPageIndex <= 0 || totalPages <= 1;
      prevBtn.classList.toggle("opacity-50", prevBtn.disabled);
      prevBtn.setAttribute("aria-disabled", String(prevBtn.disabled));
    }
    if (nextBtn) {
      nextBtn.disabled = legendPageIndex >= totalPages - 1 || totalPages <= 1;
      nextBtn.classList.toggle("opacity-50", nextBtn.disabled);
      nextBtn.setAttribute("aria-disabled", String(nextBtn.disabled));
    }
  }

  function renderLegendPage() {
    const legend = $("clinicLegend");
    if (!legend) return;

    const start = legendPageIndex * legendPageSize;
    const end = Math.min(start + legendPageSize, legendData.length);
    const slice = legendData.slice(start, end);

    const html = slice.map((r, i) => {
      const globalIndex = start + i;
      const v = legendValues[globalIndex] || 0;
      const pct = ((v / legendTotal) * 100).toFixed(1);
      const color = legendColors[globalIndex] || "hsl(0deg 0% 80%)";
      return `
        <div class="d-flex align-items-center justify-content-between">
          <div class="d-flex align-items-center gap-2">
            <span style="display:inline-block;width:12px;height:12px;border-radius:3px;background:${color}"></span>
            <span>${r.clinicName}</span>
          </div>
          <div class="text-end">
            <div class="fw-semibold">${fmtTWD.format(v)}</div>
            <div class="text-secondary small">${pct}%</div>
          </div>
        </div>`;
    }).join("");

    legend.innerHTML = html || `<div class="text-secondary small">無資料</div>`;
    updateLegendPagerUI();
  }

  function bindLegendPagerIfNeeded() {
    if (legendPagerBound) return;
    const prevBtn = $("legendPrev");
    const nextBtn = $("legendNext");
    if (prevBtn) {
      prevBtn.addEventListener("click", () => {
        if (legendPageIndex > 0) {
          legendPageIndex--;
          renderLegendPage();
        }
      });
      prevBtn.addEventListener("keydown", (e) => {
        if (e.key === "Enter" || e.key === " ") { e.preventDefault(); prevBtn.click(); }
      });
    }
    if (nextBtn) {
      nextBtn.addEventListener("click", () => {
        const maxPage = Math.max(0, Math.ceil(legendData.length / legendPageSize) - 1);
        if (legendPageIndex < maxPage) {
          legendPageIndex++;
          renderLegendPage();
        }
      });
      nextBtn.addEventListener("keydown", (e) => {
        if (e.key === "Enter" || e.key === " ") { e.preventDefault(); nextBtn.click(); }
      });
    }
    // 視窗尺寸改變時，依響應式重算每頁筆數並重繪
    window.addEventListener("resize", () => {
      const newSize = computeLegendPageSize();
      if (newSize !== legendPageSize) {
        legendPageSize = newSize;
        // 盡量維持目前看到的範圍：把目前第一筆的全域 index 換算到新 page
        const currentFirstIndex = legendPageIndex * (legendPageSize || 1);
        legendPageIndex = Math.floor(currentFirstIndex / newSize) || 0;
        renderLegendPage();
      }
    });

    legendPagerBound = true;
  }

  function genColors(n, s = 65, l = 55) {
    return Array.from({ length: n }, (_, i) => `hsl(${Math.round((360 / n) * i)}deg ${s}% ${l}%)`);
  }
  function toIsoLocalDateTime(d) {
    return `${d.getFullYear()}-${p2(d.getMonth() + 1)}-${p2(d.getDate())}T${p2(d.getHours())}:${p2(d.getMinutes())}:${p2(d.getSeconds())}`;
  }
  async function tryFetchReportAPI(start, end) {
    const qs = new URLSearchParams({ start: toIsoLocalDateTime(start), end: toIsoLocalDateTime(end) }).toString();
    const url = `${BASE_URL}/api/reports/revenue/by-clinic?${qs}`;
    try {
      const data = await fetchJSONAbs(url);
      if (Array.isArray(data) && data.length) {
        return data.map(x => ({
          clinicName: x.clinicName ?? x.clinic ?? '未命名診所',
          totalAmount: Number(x.totalAmount ?? x.amount ?? 0)
        }));
      }
    } catch (_) { }
    return null;
  }
  async function aggregateFromCases(start, end) {
    const qs = new URLSearchParams({
      receivedAtStart: toIsoLocalDateTime(start),
      receivedAtEnd: toIsoLocalDateTime(end),
      sort: "receivedAt,desc",
      page: "0",
      size: "1000"
    }).toString();

    const url = `${BASE_URL}/api/cases?${qs}`;
    const raw = await fetchJSONAbs(url);
    const list = Array.isArray(raw) ? raw : (Array.isArray(raw?.content) ? raw.content : []);

    const sumByClinic = new Map();
    for (const c of list) {
      const clinicName = c.clinicName ?? c?.clinic?.clinicName ?? c?.clinic?.name ?? '未命名診所';
      let caseAmount = 0;
      if (typeof c.totalAmount === 'number') caseAmount = c.totalAmount;
      else if (typeof c.total_amount === 'number') caseAmount = c.total_amount;
      else if (Array.isArray(c.orders)) {
        for (const o of c.orders) {
          const oa = Number(o.totalAmount ?? o.total_amount ?? 0);
          caseAmount += isNaN(oa) ? 0 : oa;
        }
      }
      sumByClinic.set(clinicName, (sumByClinic.get(clinicName) ?? 0) + (Number(caseAmount) || 0));
    }
    return Array.from(sumByClinic, ([clinicName, totalAmount]) => ({ clinicName, totalAmount }));
  }
  async function loadRevenuePie(days = 90) {
    const end = new Date(); end.setHours(23, 59, 59, 999);
    const start = new Date(); start.setDate(end.getDate() - (days - 1)); start.setHours(0, 0, 0, 0);

    let rows = await tryFetchReportAPI(start, end);
    if (!rows || !rows.length) rows = await aggregateFromCases(start, end);

    rows.sort((a, b) => b.totalAmount - a.totalAmount);
    const labels = rows.map(r => r.clinicName);
    const values = rows.map(r => Math.max(0, Number(r.totalAmount) || 0));
    const total = values.reduce((a, b) => a + b, 0) || 1;
    const colors = genColors(values.length);

    const ctx = document.getElementById('clinicRevenuePie')?.getContext('2d');
    if (!ctx) return;

    if (clinicRevenueChart) clinicRevenueChart.destroy();

    clinicRevenueChart = new Chart(ctx, {
      type: 'doughnut',
      data: { labels, datasets: [{ data: values, backgroundColor: colors, hoverOffset: 12, borderWidth: 0 }] },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        cutout: '58%',
        radius: '85%',
        layout: { padding: 10 },
        plugins: {
          legend: { display: false },
          tooltip: {
            callbacks: {
              label: (c) => {
                const v = c.parsed || 0;
                const pct = ((v / total) * 100).toFixed(1);
                return ` ${fmtTWD.format(v)}（${pct}%）`;
              },
              title: (items) => items[0]?.label ?? ''
            }
          }
        }
      }
    });

    // ✅ 右側清單改為分頁呈現（仍使用完整資料、與圖表一致）
    legendData = rows;
    legendValues = values;
    legendColors = colors;
    legendTotal = total;
    legendPageSize = computeLegendPageSize();
    legendPageIndex = 0;
    bindLegendPagerIfNeeded();
    renderLegendPage();
  }

  // ---------- 4) 最近訂單（維持先前狀態欄位顯示） ----------
  function normalizeCaseRow(x) {
    if (!x || typeof x !== "object") return null;
    const clinicName = x.clinicName ?? x?.clinic?.clinicName ?? "-";
    const dentistName = x.dentistName ?? x?.dentist?.dentistName ?? "-";
    return {
      caseNo: x.caseNo ?? x.case_no ?? "",
      patientName: x.patientName ?? x.patient_name ?? "",
      clinicName, dentistName,
      receivedAt: x.receivedAt ?? x.received_at ?? "",
      returnAt: x.returnAt ?? x.return_at ?? "",
    };
  }

  function renderStatusBadge(r) {
    const d = daysBetween(r.receivedAt, r.returnAt);
    if (d === null) return '—';
    if (d < 7) {
      return `<span class="badge rounded-pill text-bg-danger" title="預計回件-收件日：${d} 天">
                <i class="bi bi-lightning-charge-fill me-1"></i>急件
              </span>`;
    }
    return `<span class="badge rounded-pill text-bg-secondary" title="預計回件-收件日：${d} 天">一般件</span>`;
  }

  function renderRecentCases(rows) {
    const tbody = $("recentCasesTbody");
    if (!Array.isArray(rows) || rows.length === 0) {
      tbody.innerHTML = `<tr><td colspan="6" class="text-center text-muted">暫無資料</td></tr>`;
      return;
    }
    tbody.innerHTML = rows.map(r => `
      <tr>
        <td><a href="list_cases.html?caseNo=${encodeURIComponent(r.caseNo || "")}">${r.caseNo || "-"}</a></td>
        <td>${r.patientName || "-"}</td>
        <td>
          <div class="small">${r.clinicName || "-"}</div>
          <div class="text-secondary small">${r.dentistName || "-"}</div>
        </td>
        <td>${fmtDate(r.receivedAt)}</td>
        <td>${fmtDate(r.returnAt)}</td>
        <td>${renderStatusBadge(r)}</td>
      </tr>`).join("");
  }

  async function loadRecentCases() {
    const { start, end } = lastNDaysRange(90);
    const qs = new URLSearchParams({
      receivedAtStart: toIsoLocal(start),
      receivedAtEnd: toIsoLocal(end),
      sort: "receivedAt,desc",
      page: "0",
      size: "5"
    }).toString();

    try {
      const data = await fetchJSONAbs(`${BASE_URL}/api/cases?${qs}`);
      const list = Array.isArray(data) ? data : (Array.isArray(data?.content) ? data.content : []);
      const normalized = list.map(normalizeCaseRow).filter(Boolean);
      renderRecentCases(normalized);
    } catch (e) {
      console.warn("loadRecentCases:", e.message);
      renderRecentCases([]);
    }
  }

  // ---------- 5) 公告（前端靜態） ----------
  function loadNotices() {
    const list = $("noticeList");
    if (!list) return;
    list.innerHTML = `
      <li>設備維護：週四 18:00 - 22:00 暫停金屬燒結機。</li>
      <li>新版病例清單支援齒位與照片上傳欄位。</li>
      <li>提醒：出貨前請再次確認診所地址與醫師聯絡資訊。</li>`;
  }

  // ---------- 6) 時鐘 ----------
  function startClock() {
    setInterval(() => {
      const d = new Date();
      const t = `${d.getFullYear()}-${p2(d.getMonth() + 1)}-${p2(d.getDate())} ${p2(d.getHours())}:${p2(d.getMinutes())}`;
      const cell = $("nowTime"); if (cell) cell.textContent = t;
    }, 1000);
  }

  //---------- Init ----------
  document.addEventListener("DOMContentLoaded", () => {
    console.log("[home.js] BASE_URL =", BASE_URL);
    startClock();
    loadMe();
    loadKPI();          // 直接以今天所有訂單計算 新增訂單/一般件/急件
    loadRecentCases();  // 表格維持 5 筆顯示與狀態列
    loadNotices();
    loadRevenuePie(90);

    const sel = document.getElementById('revRange');
    if (sel) {
      sel.addEventListener('change', () => {
        const days = Number(sel.value || 90);
        loadRevenuePie(days);           // 會自動重置清單分頁到第 1 頁
      });
    }
  });
})();
