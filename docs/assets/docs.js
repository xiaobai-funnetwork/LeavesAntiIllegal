(() => {
  const sidebar = document.querySelector('.sidebar');
  const toggle = document.querySelector('.nav-toggle');
  const search = document.querySelector('[data-doc-search]');
  const result = document.querySelector('[data-search-result]');
  const empty = document.querySelector('[data-empty-search]');
  const sections = [...document.querySelectorAll('.doc-section')];
  const navLinks = [...document.querySelectorAll('.sidebar nav a')];

  const closeSidebar = () => {
    sidebar?.classList.remove('open');
    toggle?.setAttribute('aria-expanded', 'false');
  };

  toggle?.addEventListener('click', () => {
    const open = sidebar?.classList.toggle('open') ?? false;
    toggle.setAttribute('aria-expanded', String(open));
  });

  navLinks.forEach((link) => link.addEventListener('click', closeSidebar));

  document.addEventListener('keydown', (event) => {
    if (event.key === 'Escape') {
      closeSidebar();
    }
    if ((event.ctrlKey || event.metaKey) && event.key.toLowerCase() === 'k') {
      event.preventDefault();
      search?.focus();
    }
  });

  search?.addEventListener('input', () => {
    const query = search.value.trim().toLocaleLowerCase('zh-CN');
    let matches = 0;
    sections.forEach((section) => {
      const visible = !query || section.textContent.toLocaleLowerCase('zh-CN').includes(query);
      section.hidden = !visible;
      if (visible) matches += 1;
    });
    navLinks.forEach((link) => {
      const target = document.querySelector(link.getAttribute('href'));
      link.hidden = Boolean(query && target?.hidden);
    });
    if (result) {
      result.textContent = query ? `找到 ${matches} 个章节` : `共 ${sections.length} 个章节`;
    }
    empty?.classList.toggle('visible', matches === 0);
  });

  document.querySelectorAll('[data-copy-target]').forEach((button) => {
    button.addEventListener('click', async () => {
      const target = document.getElementById(button.dataset.copyTarget);
      if (!target) return;
      const value = target.textContent;
      try {
        await navigator.clipboard.writeText(value);
      } catch (_error) {
        const area = document.createElement('textarea');
        area.value = value;
        area.style.position = 'fixed';
        area.style.opacity = '0';
        document.body.append(area);
        area.select();
        document.execCommand('copy');
        area.remove();
      }
      const original = button.textContent;
      button.textContent = '已复制';
      window.setTimeout(() => { button.textContent = original; }, 1300);
    });
  });

  if ('IntersectionObserver' in window) {
    const observer = new IntersectionObserver((entries) => {
      entries.forEach((entry) => {
        if (!entry.isIntersecting) return;
        navLinks.forEach((link) => {
          link.classList.toggle('active', link.getAttribute('href') === `#${entry.target.id}`);
        });
      });
    }, { rootMargin: '-15% 0px -72% 0px' });
    sections.forEach((section) => observer.observe(section));
  }
})();
