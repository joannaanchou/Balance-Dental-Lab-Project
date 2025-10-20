// sidebar.js
export const sidebarTemplate = `
<aside class="app-sidebar d-flex flex-column">
  <!-- 標題 -->
  <div class="brand-title px-3 py-2 d-flex align-items-center gap-2">
    <i class="bi bi-hospital"></i>
    <span class="fw-semibold">假牙管理系統</span>
  </div>

  <!-- 可滾動區（只上下滾、透明卷軸） -->
  <div class="sidebar-scroll flex-grow-1">
    <nav class="nav flex-column">

      <!-- 首頁 -->
      <a class="nav-link" href="home.html">
        <i class="bi bi-house-door-fill me-2"></i> 首頁
      </a>

      <!-- 客戶 -->
      <a class="nav-link d-flex justify-content-between align-items-center"
         data-bs-toggle="collapse" href="#customerMenu" role="button" aria-expanded="false" aria-controls="customerMenu">
        <span><i class="bi bi-people-fill me-2"></i> 客戶管理</span>
        <i class="bi bi-chevron-down"></i>
      </a>
      <div class="collapse ps-3" id="customerMenu">
        <a class="nav-link" href="customer_management.html?view=clinicDentist">
          <i class="bi bi-link-45deg me-2"></i> 客戶總表
        </a>
        <a class="nav-link" href="customer_management.html?view=clinic">
          <i class="bi bi-hospital me-2"></i> 診所
        </a>
        <a class="nav-link" href="customer_management.html?view=dentist">
          <i class="bi bi-person me-2"></i> 醫師
        </a>
      </div>

      <!-- 產品 -->
      <a class="nav-link d-flex justify-content-between align-items-center"
         data-bs-toggle="collapse" href="#productMenu" role="button" aria-expanded="false" aria-controls="productMenu">
        <span><i class="bi bi-box me-2"></i> 產品</span>
        <i class="bi bi-chevron-down"></i>
      </a>
      <div class="collapse ps-3" id="productMenu">
        <a class="nav-link" href="product_management.html?view=detail">
          <i class="bi bi-clipboard-data me-2"></i> 產品總表
        </a>
        <a class="nav-link" href="product_management.html?view=category">
          <i class="bi bi-folder me-2"></i> 類別
        </a>
        <a class="nav-link" href="product_management.html?view=item">
          <i class="bi bi-box-seam me-2"></i> 項目
        </a>
      </div>

      <!-- 訂單 -->
      <a class="nav-link d-flex justify-content-between align-items-center"
         data-bs-toggle="collapse" href="#orderMenu" role="button" aria-expanded="false" aria-controls="orderMenu">
        <span><i class="bi bi-journal-text me-2"></i> 訂單</span>
        <i class="bi bi-chevron-down"></i>
      </a>
      <div class="collapse ps-3" id="orderMenu">
        <a class="nav-link" href="create_case.html">
          <i class="bi bi-journal-plus me-2"></i> 新增訂單／病例
        </a>
        <a class="nav-link" href="list_cases.html">
          <i class="bi bi-journal-check me-2"></i> 訂單管理
        </a>
      </div>

      <!-- 會員管理 -->
      <a class="nav-link" href="member.html">
        <i class="bi bi-person-lines-fill me-2"></i> 會員管理
      </a>

      <!-- 底部登出 -->
      <div class="mt-3 pt-3 border-top">
        <a class="nav-link text-danger" id="logoutBtn" href="#">
          <i class="bi bi-box-arrow-right me-2"></i> 登出
        </a>
      </div>
    </nav>
  </div>
</aside>
`;
