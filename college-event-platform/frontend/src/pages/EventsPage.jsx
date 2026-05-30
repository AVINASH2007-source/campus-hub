import { useState } from 'react';
import { useQuery, useMutation } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { format } from 'date-fns';
import { eventsAPI, registrationsAPI } from '../services/api';
import { useAuthStore } from '../store/authStore';

const CATEGORIES = ['ALL','ACADEMIC','CULTURAL','SPORTS','TECHNICAL','WORKSHOP','SEMINAR','HACKATHON'];

const BADGE_CLASS = {
  HACKATHON:'badge-hackathon', WORKSHOP:'badge-workshop',
  SPORTS:'badge-sports',       ACADEMIC:'badge-academic',
  CULTURAL:'badge-cultural',   TECHNICAL:'badge-technical',
  SEMINAR:'badge-seminar',     OTHER:'badge-other',
};

const CAT_EMOJI = {
  HACKATHON:'⚡', WORKSHOP:'🛠', SPORTS:'🏆', ACADEMIC:'📚',
  CULTURAL:'🎭', TECHNICAL:'💻', SEMINAR:'🎙', OTHER:'✦'
};

const BADGE_COLOR = {
  HACKATHON:'#e94560', WORKSHOP:'#c9a84c', SPORTS:'#00b4d8',
  ACADEMIC:'#7b2d8b',  CULTURAL:'#ff6b35', TECHNICAL:'#48c774',
  SEMINAR:'rgba(255,255,255,0.3)', OTHER:'rgba(255,255,255,0.2)'
};

export default function EventsPage() {
  const [search, setSearch]       = useState('');
  const [category, setCategory]   = useState('ALL');
  const [page, setPage]           = useState(0);
  const [selected, setSelected]   = useState(null);
  const canOrganize = useAuthStore(s => s.canOrganize?.() ?? false);

  const { data, isLoading } = useQuery({
    queryKey: ['events', search, category, page],
    queryFn: () => eventsAPI.list({
      search: search || undefined,
      category: category === 'ALL' ? undefined : category,
      page, size: 12, sort: 'startTime,asc',
    }).then(r => r.data),
    keepPreviousData: true,
    staleTime: 60000,
  });

  return (
    <div className="page">
      {/* Navbar */}
      <nav className="navbar anim-fade-in">
        <a href="/" className="navbar-brand">
          <div className="brand-icon">🎓</div>
          <span className="brand-name">CampusHub</span>
        </a>
        <div className="navbar-actions">
          {canOrganize && (
            <Link to="/events/create" className="btn btn-gold btn-sm">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
              New Event
            </Link>
          )}
          <Link to="/login" className="btn btn-ghost btn-sm">Sign in</Link>
        </div>
      </nav>

      {/* Hero */}
      <div className="hero anim-fade-up stagger-1">
        <div className="hero-eyebrow">
          <svg width="8" height="8" viewBox="0 0 8 8" fill="currentColor"><circle cx="4" cy="4" r="4"/></svg>
          Live Events Platform
        </div>
        <h1 className="hero-title">
          Discover <span className="accent">Campus</span><br/>Events
        </h1>
        <p className="hero-sub">Register, connect, and experience the best of college life.</p>
      </div>

      {/* Stats */}
      {data && (
        <div className="stats-bar anim-fade-up stagger-2">
          <div className="stat-item">
            <span className="stat-num">{data.totalElements}</span>
            <span className="stat-label">Total Events</span>
          </div>
          <div className="stat-divider"/>
          <div className="stat-item">
            <span className="stat-num">{CATEGORIES.length - 1}</span>
            <span className="stat-label">Categories</span>
          </div>
          <div className="stat-divider"/>
          <div className="stat-item">
            <span className="stat-num">3</span>
            <span className="stat-label">This Week</span>
          </div>
        </div>
      )}

      {/* Search */}
      <div className="search-wrap anim-fade-up stagger-2">
        <div className="search-bar">
          <svg className="search-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
          <input
            type="text"
            placeholder="Search events, topics, venues..."
            value={search}
            onChange={e => { setSearch(e.target.value); setPage(0); }}
          />
          {search && (
            <button onClick={() => setSearch('')} style={{background:'none',border:'none',color:'rgba(255,255,255,0.3)',cursor:'pointer',fontSize:20,lineHeight:1}}>×</button>
          )}
        </div>
      </div>

      {/* Categories */}
      <div className="categories anim-fade-up stagger-3">
        {CATEGORIES.map(cat => (
          <button
            key={cat}
            className={`cat-pill ${category === cat ? 'active' : ''}`}
            onClick={() => { setCategory(cat); setPage(0); }}
          >
            {cat === 'ALL' ? '✦ All' : `${CAT_EMOJI[cat]} ${cat.charAt(0)+cat.slice(1).toLowerCase()}`}
          </button>
        ))}
      </div>

      {/* Section label */}
      <div className="section-label">
        <span className="section-title">{category === 'ALL' ? 'All Events' : category.charAt(0)+category.slice(1).toLowerCase()}</span>
        {data && <span className="section-count">{data.totalElements} result{data.totalElements !== 1 ? 's' : ''}</span>}
      </div>

      {/* Grid */}
      <div className="events-grid">
        {isLoading && Array.from({length:6}).map((_,i) => <SkeletonCard key={i}/>)}
        {!isLoading && data?.content?.length === 0 && (
          <div className="empty-state">
            <div className="empty-icon">🔍</div>
            <div className="empty-title">No events found</div>
            <div className="empty-sub">Try a different search or category</div>
          </div>
        )}
        {!isLoading && data?.content?.map((event, i) => (
          <EventCard key={event.id} event={event} index={i} onClick={() => setSelected(event)} />
        ))}
      </div>

      {/* Pagination */}
      {data && data.totalPages > 1 && (
        <div className="pagination">
          <button className="page-btn" onClick={() => setPage(p=>p-1)} disabled={page===0}>
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><polyline points="15 18 9 12 15 6"/></svg>
          </button>
          {Array.from({length: Math.min(data.totalPages,7)}).map((_,i) => (
            <button key={i} className={`page-btn ${i===page?'active':''}`} onClick={() => setPage(i)}>{i+1}</button>
          ))}
          <button className="page-btn" onClick={() => setPage(p=>p+1)} disabled={page===data.totalPages-1}>
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><polyline points="9 18 15 12 9 6"/></svg>
          </button>
        </div>
      )}

      {/* Modal */}
      {selected && (
        <EventModal event={selected} onClose={() => setSelected(null)} />
      )}
    </div>
  );
}

// ── Event Card ────────────────────────────────────────────────

function EventCard({ event, index, onClick }) {
  const isFull  = event.maxParticipants && event.registeredCount >= event.maxParticipants;
  const spots   = event.maxParticipants ? event.maxParticipants - event.registeredCount : null;

  return (
    <div
      className="event-card anim-fade-up"
      style={{animationDelay:`${index*0.06}s`,opacity:0,cursor:'pointer'}}
      onClick={onClick}
    >
      <div className="card-banner">
        {event.bannerImageUrl
          ? <img src={event.bannerImageUrl} alt={event.title}/>
          : <div className="banner-placeholder">{CAT_EMOJI[event.category]||'✦'}</div>
        }
        <span className={`card-cat-badge ${BADGE_CLASS[event.category]||'badge-other'}`}>{event.category}</span>
        {isFull && <span className="full-badge">Full</span>}
      </div>
      <div className="card-body">
        <div className="card-title">{event.title}</div>
        <div className="card-meta">
          <div className="meta-row">
            <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
            {format(new Date(event.startTime),'EEE, MMM d · h:mm a')}
          </div>
          <div className="meta-row">
            <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0118 0z"/><circle cx="12" cy="10" r="3"/></svg>
            {event.venue}
          </div>
          {event.maxParticipants && (
            <div className="meta-row">
              <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2"/><circle cx="9" cy="7" r="4"/></svg>
              {isFull ? 'Fully booked' : `${spots} spot${spots!==1?'s':''} left`}
            </div>
          )}
        </div>
        <div className="card-footer">
          <span className={`card-price ${event.fee===0||event.fee==='0.00'?'free':''}`}>
            {event.fee===0||event.fee==='0.00'?'Free':`₹${event.fee}`}
          </span>
          <div className="card-arrow">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><line x1="5" y1="12" x2="19" y2="12"/><polyline points="12 5 19 12 12 19"/></svg>
          </div>
        </div>
      </div>
    </div>
  );
}

// ── Event Modal ───────────────────────────────────────────────

function EventModal({ event, onClose }) {
  const isAuth  = useAuthStore(s => s.isAuthenticated);
  const navigate = typeof window !== 'undefined' ? null : null;
  const [toast, setToast] = useState(null);

  const isFull    = event.maxParticipants && event.registeredCount >= event.maxParticipants;
  const spotsLeft = event.maxParticipants ? event.maxParticipants - event.registeredCount : null;
  const progress  = event.maxParticipants ? (event.registeredCount / event.maxParticipants) * 100 : 0;

  const registerMutation = useMutation({
    mutationFn: () => registrationsAPI.register(event.id),
    onSuccess: () => {
      setToast({ type:'success', msg:'🎉 Registered successfully!' });
      setTimeout(() => setToast(null), 3000);
    },
    onError: err => {
      setToast({ type:'error', msg: err.response?.data?.message || 'Registration failed.' });
      setTimeout(() => setToast(null), 3000);
    },
  });

  // Close on backdrop click
  const handleBackdrop = e => { if (e.target === e.currentTarget) onClose(); };

  // Close on Escape key
  useState(() => {
    const handler = e => { if (e.key === 'Escape') onClose(); };
    window.addEventListener('keydown', handler);
    return () => window.removeEventListener('keydown', handler);
  });

  return (
    <div
      onClick={handleBackdrop}
      style={{
        position:'fixed',inset:0,zIndex:500,
        background:'rgba(0,0,0,0.75)',
        backdropFilter:'blur(8px)',
        display:'flex',alignItems:'center',justifyContent:'center',
        padding:24,
        animation:'fadeIn 0.2s ease',
      }}
    >
      <div style={{
        width:'100%',maxWidth:720,maxHeight:'90vh',
        background:'linear-gradient(145deg,#0e0e18,#12121e)',
        border:'1px solid rgba(255,255,255,0.08)',
        borderRadius:'var(--radius-xl)',
        overflow:'hidden',
        display:'flex',flexDirection:'column',
        animation:'fadeUp 0.3s ease',
        boxShadow:'0 40px 120px rgba(0,0,0,0.8)',
        position:'relative',
      }}>

        {/* Banner */}
        <div style={{
          height:220,flexShrink:0,
          background: event.bannerImageUrl
            ? `url(${event.bannerImageUrl}) center/cover`
            : 'linear-gradient(135deg,#1a1a2e,#16213e,#0f3460)',
          position:'relative',
          display:'flex',alignItems:'center',justifyContent:'center',
        }}>
          <div style={{position:'absolute',inset:0,background:'linear-gradient(to bottom,transparent 40%,rgba(14,14,24,1) 100%)'}}/>

          {!event.bannerImageUrl && (
            <div style={{fontSize:64,opacity:0.15,position:'relative',zIndex:1}}>
              {CAT_EMOJI[event.category]||'✦'}
            </div>
          )}

          {/* Category */}
          <div style={{
            position:'absolute',top:20,left:20,
            padding:'5px 14px',
            background: BADGE_COLOR[event.category]||BADGE_COLOR.OTHER,
            borderRadius:'var(--radius-full)',
            fontSize:10,fontWeight:700,letterSpacing:1.5,textTransform:'uppercase',
            color:['WORKSHOP','SPORTS','TECHNICAL'].includes(event.category)?'#000':'#fff',
          }}>
            {event.category}
          </div>

          {/* Close button */}
          <button
            onClick={onClose}
            style={{
              position:'absolute',top:16,right:16,
              width:36,height:36,borderRadius:'50%',
              background:'rgba(0,0,0,0.5)',
              border:'1px solid rgba(255,255,255,0.15)',
              color:'rgba(255,255,255,0.8)',
              cursor:'pointer',fontSize:18,
              display:'flex',alignItems:'center',justifyContent:'center',
              transition:'var(--transition)',backdropFilter:'blur(4px)',
            }}
          >×</button>
        </div>

        {/* Scrollable content */}
        <div style={{overflowY:'auto',flex:1,padding:'4px 32px 32px'}}>

          {/* Toast */}
          {toast && (
            <div style={{
              padding:'10px 16px',marginBottom:16,
              background: toast.type==='success'?'rgba(72,199,116,0.12)':'rgba(233,69,96,0.12)',
              border:`1px solid ${toast.type==='success'?'rgba(72,199,116,0.3)':'rgba(233,69,96,0.3)'}`,
              borderRadius:'var(--radius-md)',
              fontSize:13,color:toast.type==='success'?'#48c774':'#e94560',
            }}>
              {toast.msg}
            </div>
          )}

          <h2 style={{
            fontFamily:'var(--font-display)',
            fontSize:'clamp(22px,3vw,32px)',
            fontWeight:700,letterSpacing:-0.5,
            lineHeight:1.15,marginBottom:8,
          }}>
            {event.title}
          </h2>

          <div style={{fontSize:13,color:'rgba(255,255,255,0.35)',marginBottom:24}}>
            By <span style={{color:'var(--gold-light)'}}>{event.organizerName}</span>
          </div>

          {/* Info grid */}
          <div style={{display:'grid',gridTemplateColumns:'1fr 1fr',gap:12,marginBottom:24}}>
            {[
              {icon:'📅', label:'Date',  val: format(new Date(event.startTime),'EEE, MMM d, yyyy')},
              {icon:'⏰', label:'Time',  val: `${format(new Date(event.startTime),'h:mm a')} – ${format(new Date(event.endTime),'h:mm a')}`},
              {icon:'📍', label:'Venue', val: event.venue},
              {icon:'💰', label:'Fee',   val: event.fee===0||event.fee==='0.00'?'Free Entry':`₹${event.fee}`},
            ].map((item,i) => (
              <div key={i} style={{
                padding:'14px 16px',
                background:'rgba(255,255,255,0.03)',
                border:'1px solid rgba(255,255,255,0.06)',
                borderRadius:'var(--radius-md)',
              }}>
                <div style={{fontSize:16,marginBottom:4}}>{item.icon}</div>
                <div style={{fontSize:10,color:'rgba(255,255,255,0.3)',letterSpacing:0.8,textTransform:'uppercase',marginBottom:3}}>{item.label}</div>
                <div style={{fontSize:13,fontWeight:500,color:'var(--white)'}}>{item.val}</div>
              </div>
            ))}
          </div>

          {/* Description */}
          {event.description && (
            <p style={{fontSize:14,lineHeight:1.8,color:'rgba(255,255,255,0.45)',marginBottom:24}}>
              {event.description}
            </p>
          )}

          {/* Tags */}
          {event.tags && (
            <div style={{display:'flex',gap:8,flexWrap:'wrap',marginBottom:24}}>
              {event.tags.split(',').map(tag => (
                <span key={tag} style={{
                  padding:'4px 12px',
                  background:'rgba(201,168,76,0.08)',
                  border:'1px solid rgba(201,168,76,0.2)',
                  borderRadius:'var(--radius-full)',
                  fontSize:12,color:'var(--gold)',
                }}>#{tag.trim()}</span>
              ))}
            </div>
          )}

          {/* Capacity bar */}
          {event.maxParticipants && (
            <div style={{marginBottom:24}}>
              <div style={{display:'flex',justifyContent:'space-between',fontSize:13,marginBottom:8}}>
                <span style={{color:'rgba(255,255,255,0.4)'}}>
                  {event.registeredCount} / {event.maxParticipants} registered
                </span>
                <span style={{color: isFull?'var(--electric)':'var(--gold)'}}>
                  {isFull?'Fully booked':`${spotsLeft} spots left`}
                </span>
              </div>
              <div style={{height:6,background:'rgba(255,255,255,0.07)',borderRadius:3,overflow:'hidden'}}>
                <div style={{
                  height:'100%',
                  width:`${Math.min(progress,100)}%`,
                  background: progress>80
                    ?'linear-gradient(90deg,var(--electric),var(--electric-soft))'
                    :'linear-gradient(90deg,var(--gold),var(--gold-light))',
                  borderRadius:3,
                  transition:'width 1s ease',
                }}/>
              </div>
            </div>
          )}

          {/* Register button */}
          <button
            onClick={() => {
              if (!isAuth) { window.location.href='/login'; return; }
              registerMutation.mutate();
            }}
            disabled={isFull || registerMutation.isPending}
            className={`btn btn-lg ${isFull?'btn-ghost':'btn-gold'}`}
            style={{width:'100%',justifyContent:'center',opacity:isFull?0.5:1}}
          >
            {registerMutation.isPending
              ? 'Registering...'
              : isFull
                ? 'Event Full'
                : isAuth
                  ? '✓ Register for this Event'
                  : '→ Sign in to Register'
            }
          </button>

          {!isAuth && (
            <p style={{textAlign:'center',fontSize:12,color:'rgba(255,255,255,0.25)',marginTop:10}}>
              You need an account to register for events
            </p>
          )}
        </div>
      </div>
    </div>
  );
}

// ── Skeleton ──────────────────────────────────────────────────

function SkeletonCard() {
  return (
    <div className="event-card" style={{pointerEvents:'none'}}>
      <div className="card-banner skeleton"/>
      <div className="card-body" style={{gap:14}}>
        <div className="skeleton" style={{height:20,width:'75%',borderRadius:4}}/>
        <div className="skeleton" style={{height:14,width:'55%',borderRadius:4}}/>
        <div className="skeleton" style={{height:14,width:'65%',borderRadius:4}}/>
        <div style={{display:'flex',justifyContent:'space-between',paddingTop:12,borderTop:'1px solid rgba(255,255,255,0.06)'}}>
          <div className="skeleton" style={{height:18,width:60,borderRadius:4}}/>
          <div className="skeleton" style={{height:32,width:32,borderRadius:'50%'}}/>
        </div>
      </div>
    </div>
  );
}