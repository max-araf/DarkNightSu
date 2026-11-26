from playwright.sync_api import sync_playwright
U='file:///tmp/claude-0/-home-claude/94d55e0d-96e6-5078-91d4-43c3651d8507/scratchpad/v13/kit/site/index.html'
with sync_playwright() as p:
    b=p.chromium.launch()
    for n,w,h,pal in [('d',1280,800,2),('m',390,844,0)]:
        pg=b.new_page(viewport={'width':w,'height':h}); errs=[]
        pg.on('pageerror',lambda e:errs.append(str(e)))
        pg.goto(U); pg.evaluate(f"setPal({pal})"); pg.wait_for_timeout(2800)
        pg.screenshot(path=f'../{n}_hero.png')
        pg.evaluate("document.querySelectorAll('.rv').forEach(e=>{e.classList.add('in');e.style.transitionDelay='0ms'})"); pg.wait_for_timeout(1000)
        pg.screenshot(path=f'../{n}_full.png',full_page=True); print(n,errs)
    b.close()
