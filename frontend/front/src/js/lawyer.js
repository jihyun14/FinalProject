// ===================================
// 7. 변호사 페이지 (DB 연동 버전)
// ===================================

import { showToast } from "./utils.js";
import { getLawyerList } from "./api.js"; // 👈 추가된 API 함수 임포트

// ===== 1) 상태 관리 =====
// 서버에서 받아온 원본 데이터를 저장할 변수
let dbLawyers = [];

// 화면 필터링용 상태
const lawyerState = {
  query: "",
  region: "ALL",
};

// ===== 2) 유틸리티: DB 데이터를 화면 데이터로 변환 =====

/**
 * DB의 officeLocation(예: "서울 송파구")을 기반으로
 * 필터 카테고리(예: "서울·수도권")를 반환하는 함수
 */
function getRegionCategory(location) {
  if (!location) return "기타";

  if (
    location.includes("서울") ||
    location.includes("경기") ||
    location.includes("인천")
  ) {
    return "서울·수도권";
  } else if (
    location.includes("부산") ||
    location.includes("대구") ||
    location.includes("울산") ||
    location.includes("경상") ||
    location.includes("경남") ||
    location.includes("경북")
  ) {
    return "부산·영남권";
  } else if (
    location.includes("대전") ||
    location.includes("충청") ||
    location.includes("충남") ||
    location.includes("충북") ||
    location.includes("세종")
  ) {
    return "대전·충청권";
  } else if (
    location.includes("광주") ||
    location.includes("전라") ||
    location.includes("전남") ||
    location.includes("전북") ||
    location.includes("제주")
  ) {
    return "광주·전라·제주권";
  }
  return "기타";
}

/**
 * DB 데이터를 화면 렌더링용 구조로 변환
 * SQL 컬럼 -> JS 객체 매핑
 */
function formatLawyerData(list) {
  // 1. 지역별 그룹핑을 위한 기본 구조
  const grouped = {
    "서울·수도권": [],
    "부산·영남권": [],
    "대전·충청권": [],
    "광주·전라·제주권": [],
    기타: [],
  };

  list.forEach((item) => {
    const category = getRegionCategory(item.officeLocation);

    // DB 컬럼과 화면 표시 데이터 매핑
    const formattedItem = {
      type: "law", // 아이콘 타입
      name: item.name, // 변호사 이름
      tags: item.detailSpecialty ? [item.detailSpecialty] : [], // 전문분야 -> 태그
      phone: item.contact, // 연락처
      address: `${item.office} (${item.officeLocation})`, // 사무실명 + 위치
      originLocation: item.officeLocation, // 검색용 원본 위치
      url: "#", // 상세 페이지 URL (추후 구현 시 id 활용 가능)
      note: item.description, // 한줄 소개
    };

    if (grouped[category]) {
      grouped[category].push(formattedItem);
    } else {
      // 예외 처리: 정의되지 않은 지역일 경우 기타로
      grouped["기타"].push(formattedItem);
    }
  });

  // 2. 렌더링 함수가 사용하는 배열 형태(region, items)로 변환
  return Object.keys(grouped)
    .map((key) => ({
      region: key,
      items: grouped[key],
    }))
    .filter((group) => group.items.length > 0); // 데이터가 있는 지역만 필터링
}

// ===== 3) 렌더링 관련 함수들 =====

function makeTag(text) {
  const s = document.createElement("span");
  s.className = "tag";
  s.textContent = text;
  return s;
}

function card(item) {
  const cardTpl = document.getElementById("card-tpl");
  if (!cardTpl) return document.createElement("div");

  const $ = cardTpl.content.firstElementChild.cloneNode(true);
  const icon = $.querySelector(".card__icon");
  const title = $.querySelector(".card__title");
  const meta = $.querySelector(".card__meta");
  const desc = $.querySelector(".desc");
  const actions = $.querySelector(".actions");

  icon.classList.add("law");
  icon.textContent = "법";

  title.textContent = item.name;
  meta.innerHTML = "";
  meta.appendChild(makeTag("변호사")); // 기본 태그

  if (item.phone) meta.appendChild(makeTag(item.phone));

  // tags 배열 처리
  if (Array.isArray(item.tags)) {
    item.tags.forEach((t) => meta.appendChild(makeTag("#" + t)));
  }

  // 주소 표시 (사무실명 등)
  if (item.address) {
    const addrSpan = document.createElement("span");
    addrSpan.className = "meta-addr";
    addrSpan.textContent = " " + item.address;
    addrSpan.style.fontSize = "0.85rem";
    addrSpan.style.color = "#666";
    meta.appendChild(addrSpan);
  }

  desc.textContent = item.note || "";

  // 버튼 액션들
  const aCall = document.createElement("a");
  aCall.className = "btn small line";
  aCall.href = item.phone ? `tel:${item.phone.replaceAll(/[^0-9]/g, "")}` : "#";
  aCall.textContent = "전화";

  const aCopy = document.createElement("button");
  aCopy.className = "btn small";
  aCopy.textContent = "연락처 복사";
  aCopy.addEventListener("click", async () => {
    try {
      await navigator.clipboard.writeText(item.phone || "");
      aCopy.textContent = "복사됨!";
      setTimeout(() => (aCopy.textContent = "연락처 복사"), 1200);
    } catch (e) {
      showToast("클립보드 복사 실패", "error");
    }
  });

  actions.append(aCall, aCopy);
  return $;
}

export function render() {
  const app = document.getElementById("app");
  const sectionTpl = document.getElementById("section-tpl");

  if (!app || !sectionTpl) return;

  app.innerHTML = "";

  // 1. 포맷팅된 데이터 가져오기
  const formattedData = formatLawyerData(dbLawyers);

  // 2. 지역 필터링
  const regions =
    lawyerState.region === "ALL"
      ? formattedData
      : formattedData.filter((r) => r.region === lawyerState.region);

  regions.forEach((block) => {
    const sec = sectionTpl.content.firstElementChild.cloneNode(true);
    sec.querySelector("h2").textContent = block.region;

    const grid = sec.querySelector("[data-grid]");

    // 3. 검색어(이름, 주소, 전문분야 등) 필터링
    const list = block.items.filter((it) => {
      const q = lawyerState.query.trim().toLowerCase();
      const hay = [it.name, it.address, (it.tags || []).join(","), it.note]
        .filter(Boolean)
        .join(" ")
        .toLowerCase();
      return !q || hay.includes(q);
    });

    sec.querySelector(".count").textContent = `${list.length}명`;

    if (list.length === 0) {
      // 검색 결과 없음 처리 (해당 섹션 렌더링 안함 or 빈 메시지)
      if (lawyerState.query) {
        // 검색 중일때만 빈 섹션을 숨기거나 메시지 표시
        return;
      }
    }

    if (list.length > 0) {
      list.forEach((it) => grid.appendChild(card(it)));
      app.appendChild(sec);
    }
  });

  // 전체 결과가 하나도 없을 때
  if (app.children.length === 0) {
    app.innerHTML = `<div class="empty" style="padding:2rem; text-align:center;">조건에 맞는 변호사를 찾을 수 없습니다.</div>`;
  }
}

// ===== 4) CSV 내보내기 =====
function toCSV(rows) {
  const header = ["region", "name", "phone", "address", "specialty", "note"];
  const lines = [header.join(",")];
  rows.forEach((r) => {
    const vals = [
      r.region,
      r.name,
      r.phone,
      r.address,
      r.tags.join("|"),
      r.note,
    ].map((v) => '"' + String(v || "").replaceAll('"', '""') + '"');
    lines.push(vals.join(","));
  });
  return lines.join("\n");
}

function exportCSV() {
  const all = [];
  const formattedData = formatLawyerData(dbLawyers);

  const regions =
    lawyerState.region === "ALL"
      ? formattedData
      : formattedData.filter((r) => r.region === lawyerState.region);

  regions.forEach((r) => {
    r.items.forEach((it) => {
      // 현재 화면에 필터링된 것만 내보낼지 여부에 따라 로직 조정 가능
      // 여기서는 검색어 필터까지 적용
      const q = lawyerState.query.trim().toLowerCase();
      const hay = [it.name, it.address, (it.tags || []).join(","), it.note]
        .join(" ")
        .toLowerCase();

      if (!q || hay.includes(q)) {
        all.push({ region: r.region, ...it });
      }
    });
  });

  const blob = new Blob(["\uFEFF" + toCSV(all)], {
    type: "text/csv;charset=utf-8;",
  });
  const url = URL.createObjectURL(blob);
  const a = document.createElement("a");
  a.href = url;
  a.download = "lawyers_export.csv";
  a.click();
  URL.revokeObjectURL(url);
}

// ===== 5) 초기화 함수 =====
export async function initLawyerPageListeners() {
  // 1. 이벤트 리스너 등록
  document.getElementById("q")?.addEventListener("input", (e) => {
    lawyerState.query = e.target.value;
    render();
  });

  document.getElementById("type")?.addEventListener("change", (e) => {
    lawyerState.region = e.target.value;
    render();
  });

  document.getElementById("resetBtn")?.addEventListener("click", () => {
    lawyerState.query = "";
    lawyerState.region = "ALL";
    document.getElementById("q").value = "";
    document.getElementById("type").value = "ALL";
    render();
  });

  document.getElementById("exportBtn")?.addEventListener("click", exportCSV);

  // 2. DB 데이터 불러오기
  try {
    const data = await getLawyerList();
    if (Array.isArray(data)) {
      dbLawyers = data; // 전역 변수에 저장
      render(); // 화면 그리기
    }
  } catch (err) {
    console.error("변호사 데이터 로드 실패:", err);
    showToast("변호사 정보를 불러오지 못했습니다.", "error");
  }
}
