import React from 'react';
import { useLanguage } from '../context/LanguageContext';

const services = [
  ['location', 'homeFindRoute', 'homeFindRouteDesc', '#routes'],
  ['card', 'homeComparePasses', 'homeComparePassesDesc', '#passes'],
  ['upload', 'homeApplyOnline', 'homeApplyOnlineDesc', '/signin'],
  ['download', 'homeDigitalPass', 'homeDigitalPassDesc', '/signin'],
];

const routes = [
  ['11', 'Swargate', 'Hinjewadi', 'Deccan, Kothrud, Wakad', '₹35'],
  ['16', 'Shivajinagar', 'Pimpri', 'Aundh, Akurdi, Chinchwad', '₹30'],
  ['50', 'Pune Station', 'Hadapsar', 'Swargate, Market Yard, Fatima Nagar', '₹20'],
  ['105', 'Katraj', 'Bhosari', 'Swargate, Camp, Yerwada', '₹40'],
  ['7', 'Deccan', 'Kharadi', 'Pune University, Viman Nagar, EON IT Park', '₹25'],
  ['152', 'Nigdi', 'Swargate', 'Pimpri, Shivajinagar, Deccan', '₹45'],
];

const passes = [
  ['General Passenger', 'PMC & PCMC Area Daily Pass', '₹70', '24 hours', 'PMC + PCMC', 'None required'],
  ['General Passenger', 'All-PMPML / PMRDA Daily Pass', '₹150', '24 hours', 'PMPML + PMRDA', 'None required'],
  ['General Passenger', 'General Monthly Pass', '₹1,500', '30 days', 'PMPML network', 'Photo ID'],
  ['Student', 'Monthly Student Pass', '₹750', '30 days', 'PMPML network', 'College ID + bonafide'],
  ['Student', 'Quarterly Student Pass', '₹3,000', '90 days', 'PMPML network', 'College ID + bonafide'],
  ['Student', 'Annual Student Pass', '₹5,000', '365 days', 'PMPML network', 'College ID + bonafide'],
  ['Senior Citizen', 'Senior Citizen Monthly Pass', 'Fare on confirmation', '30 days', 'PMPML network', 'Age proof / Aadhaar'],
  ['Concessionary', 'Person with Disability Pass', 'Subsidized fare', 'Per rules', 'PMPML network', 'Disability certificate / UDID'],
  ['Concessionary', 'Visually Impaired Passenger Pass', 'Free travel', 'Per rules', 'PMPML network', 'Medical certificate'],
  ['Concessionary', 'Journalist / Reporter Pass', 'Media quota fare', 'Per rules', 'PMPML network', 'Press accreditation'],
  ['Concessionary', 'Freedom Fighter Pass', 'Free (honorary)', 'Lifetime', 'PMPML network', 'Government certificate'],
];

const tagClass = {
  'General Passenger': 'home-tag-general',
  Student: 'home-tag-student',
  'Senior Citizen': 'home-tag-senior',
  Concessionary: 'home-tag-concessionary',
};

function ServiceIcon({ type }) {
  const paths = {
    location: <><path d="M12 21s7-6.1 7-12a7 7 0 1 0-14 0c0 5.9 7 12 7 12Z" /><circle cx="12" cy="9" r="2.25" /></>,
    card: <><rect x="3.5" y="5.5" width="17" height="13" rx="1.5" /><path d="M3.5 9h17" /><path d="M7 14h4" /></>,
    upload: <><path d="M12 16V4" /><path d="m7.5 8.5 4.5-4.5 4.5 4.5" /><path d="M5 14v4.5h14V14" /></>,
    download: <><path d="M12 4v12" /><path d="m7.5 11.5 4.5 4.5 4.5-4.5" /><path d="M5 19.5h14" /></>,
  };

  return (
    <svg className="home-service-svg" viewBox="0 0 24 24" aria-hidden="true" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
      {paths[type]}
    </svg>
  );
}

function CitizenIcon({ type }) {
  const icons = {
    shield: <><path d="M12 3.5 18 6v5.2c0 4-2.5 7.2-6 9.3-3.5-2.1-6-5.3-6-9.3V6l6-2.5Z" /><path d="m9.4 12 1.7 1.7 3.6-3.6" /></>,
    lock: <><rect x="5.5" y="10" width="13" height="10" rx="1.5" /><path d="M8.5 10V7.5a3.5 3.5 0 0 1 7 0V10" /></>,
    document: <><path d="M7 3.5h7l3 3V20H7z" /><path d="M14 3.5V7h3M9.5 11h5M9.5 14h5M9.5 17h3" /></>,
    headset: <><path d="M4.5 13v-1a7.5 7.5 0 0 1 15 0v1" /><path d="M4.5 13.5v3a2 2 0 0 0 2 2h1v-6h-1a2 2 0 0 0-2 2ZM19.5 13.5v3a2 2 0 0 1-2 2h-1v-6h1a2 2 0 0 1 2 2Z" /></>,
  };

  return (
    <svg className="home-citizen-svg" viewBox="0 0 24 24" aria-hidden="true" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round">
      {icons[type]}
    </svg>
  );
}

function BusIcon() {
  return (
    <svg className="home-bus-svg" viewBox="0 0 24 24" aria-hidden="true" fill="none" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round">
      <path d="M5 16.5V6.8C5 5.25 6.25 4 7.8 4h8.4C17.75 4 19 5.25 19 6.8v9.7M4 16.5h16" />
      <path d="M7 19h2m6 0h2M7 8h10M8 12h2m4 0h2" />
      <circle cx="8" cy="16.5" r="1" /><circle cx="16" cy="16.5" r="1" />
    </svg>
  );
}

export function HomePage() {
  const { t } = useLanguage();
  const steps = ['homeStep1', 'homeStep2', 'homeStep3', 'homeStep4'];

  return (
    <main className="home-page">
      <section id="home" className="home-hero">
        <div className="home-container home-hero-grid">
          <div className="home-hero-copy">
            <span className="home-eyebrow">{t('homeEyebrow')}</span>
            <h1>{t('homeHeroTitle')}</h1>
            <p>{t('homeHeroDescription')}</p>
            <div className="home-actions">
              <a className="home-button home-button-primary" href="#passes">{t('homeExplorePasses')}</a>
              <a className="home-button home-button-outline" href="#routes">{t('homeViewRoutes')}</a>
            </div>
          </div>
          <div className="home-hero-image">
            <img src="/images/pmpml-homepage.png" alt="PMPML bus serving passengers in Pune" />
          </div>
        </div>
      </section>

      <section className="home-section home-services">
        <div className="home-container">
          <h2>{t('homePublicServices')}</h2>
          <div className="home-service-grid">
            {services.map(([icon, titleKey, descriptionKey, href]) => (
              <a className="home-service-card" href={href} key={titleKey}>
                <span className="home-service-icon"><ServiceIcon type={icon} /></span>
                <span className="home-service-title">{t(titleKey)}</span>
                <span className="home-service-description">{t(descriptionKey)}</span>
              </a>
            ))}
          </div>
        </div>
      </section>

      <section id="routes" className="home-section home-routes">
        <div className="home-container">
          <div className="home-section-heading">
            <div>
              <h2>{t('homeRoutesTitle')}</h2>
              <p>{t('homeRoutesSubtitle')}</p>
            </div>
          </div>
          <div className="home-route-table">
            <div className="home-route-row home-route-heading">
              <span>{t('homeRoute')}</span><span>{t('homeFrom')}</span><span>{t('homeTo')}</span><span>{t('homeKeyStops')}</span><span>{t('homeFare')}</span>
            </div>
            {routes.map(([number, from, to, stops, fare]) => (
              <div className="home-route-row" key={number}>
                <strong className="home-route-number">{number}</strong>
                <span>{from}</span><span>{to}</span><span className="home-route-stops">{stops}</span>
                <strong className="home-route-fare">{fare}</strong>
              </div>
            ))}
          </div>
        </div>
      </section>

      <section id="passes" className="home-section home-passes">
        <div className="home-container">
          <div className="home-section-heading">
            <div>
              <h2>{t('homePassesTitle')}</h2>
              <p>{t('homePassesSubtitle')}</p>
            </div>
          </div>
          <div className="home-pass-grid">
            {passes.map(([group, name, price, duration, area, documents]) => (
              <article className="home-pass-card" key={name}>
                <span className={`home-pass-tag ${tagClass[group]}`}>{group}</span>
                <h3>{name}</h3>
                <dl>
                  <div><dt>{t('homePrice')}</dt><dd>{price}</dd></div>
                  <div><dt>{t('homeValid')}</dt><dd>{duration}</dd></div>
                  <div><dt>{t('homeArea')}</dt><dd>{area}</dd></div>
                  <div><dt>{t('homeDocuments')}</dt><dd>{documents}</dd></div>
                </dl>
                <a href="/signin">{t('homeApplyNow')} <span aria-hidden="true">→</span></a>
              </article>
            ))}
          </div>
        </div>
      </section>

      <section className="home-section home-steps">
        <div className="home-container">
          <h2>{t('homeHowItWorks')}</h2>
          <p className="home-section-intro">{t('homeHowItWorksSubtitle')}</p>
          <div className="home-step-grid">
            {steps.map((stepKey, index) => (
              <div className="home-step" key={stepKey}>
                <span>{String(index + 1).padStart(2, '0')}</span>
                <h3>{t(stepKey)}</h3>
                <p>{[
                  'Browse PMPML routes and select the pass category that fits your travel needs and eligibility.',
                  'Review required documents before starting your application.',
                  'Fill in your details, upload scanned documents, and submit securely.',
                  'Monitor verification status and download the approved PDF pass.',
                ][index]}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      <section className="home-trust">
        <div className="home-container home-trust-grid">
          <div className="home-citizen-panel">
            <h2>Designed for Citizens</h2>
            <div className="home-citizen-grid">
              {[
                ['shield', 'Verified Pass Information', 'All pass types, prices, and eligibility details are sourced directly from official PMPML rules.'],
                ['lock', 'Secure Sign-In', 'Your account and application data are protected.'],
                ['document', 'Document Requirements Shown First', 'Check what documents you need before starting an application.'],
                ['headset', 'Citizen Support', 'Get help with routes, passes, and application status.'],
              ].map(([icon, title, description]) => (
                <article className="home-citizen-card" key={title}>
                  <span className="home-citizen-icon"><CitizenIcon type={icon} /></span>
                  <div><h3>{title}</h3><p>{description}</p></div>
                </article>
              ))}
            </div>
          </div>
          <div id="about" className="home-about-card">
            <h2>{t('homeAboutEyebrow')}</h2>
            <div>
              <p><strong>Pune Mahanagar Parivahan Mahamandal Ltd. (PMPML)</strong> operates the public bus transport network serving Pune and Pimpri-Chinchwad, providing daily mobility to lakhs of citizens.</p>
              <p>PMPML services cover PMC, PCMC, PMRDA suburban corridors, metro feeder routes, and connected commuter routes.</p>
              <p>TransitEase helps passengers discover routes, compare passes, submit applications, and receive a verified digital pass.</p>
            </div>
          </div>
        </div>
      </section>
      <footer className="home-footer">
        <div className="home-container home-footer-grid">
          <div>
            <div className="home-footer-brand"><span className="home-brand-mark"><BusIcon /></span><strong>PMPML TransitEase</strong></div>
            <p>The official digital pass platform for PMPML — helping citizens discover routes, apply for bus passes, and travel with confidence.</p>
          </div>
          <div className="home-footer-links">
            <strong>Quick Links</strong>
            <a href="#routes">Routes</a>
            <a href="#passes">Passes</a>
            <a href="#about">About PMPML</a>
            <a href="/signin">Sign In</a>
          </div>
        </div>
        <div className="home-footer-bottom">© 2026 PMPML TransitEase. Digital Citizen Services.</div>
      </footer>
    </main>
  );
}
