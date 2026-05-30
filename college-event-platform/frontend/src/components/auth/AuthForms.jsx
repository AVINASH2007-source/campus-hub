import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useNavigate, Link } from 'react-router-dom';
import { useMutation } from '@tanstack/react-query';
import { authAPI } from '../../services/api';
import { useAuthStore } from '../../store/authStore';

const loginSchema = z.object({
  email: z.string().email('Enter a valid email'),
  password: z.string().min(1, 'Password is required'),
});

const registerSchema = z.object({
  firstName:       z.string().min(2, 'Min 2 characters'),
  lastName:        z.string().min(2, 'Min 2 characters'),
  email:           z.string().email('Enter a valid email'),
  rollNumber:      z.string().optional(),
  department:      z.string().min(2, 'Select department'),
  password:        z.string().min(8, 'Min 8 characters'),
  confirmPassword: z.string(),
}).refine(d => d.password === d.confirmPassword, {
  message: 'Passwords do not match', path: ['confirmPassword'],
});

const DEPARTMENTS = [
  'Computer Science','Information Technology','Electronics & Communication',
  'Mechanical Engineering','Civil Engineering','Business Administration',
  'Arts & Humanities','Science','Other',
];

// ── Login ─────────────────────────────────────────────────────

export function LoginForm() {
  const [showPwd, setShowPwd] = useState(false);
  const [toast, setToast]     = useState(null);
  const navigate  = useNavigate();
  const setAuth   = useAuthStore(s => s.setAuth);

  const { register, handleSubmit, formState: { errors } } = useForm({
    resolver: zodResolver(loginSchema),
  });

  const mutation = useMutation({
    mutationFn: d => authAPI.login(d).then(r => r.data),
    onSuccess: data => {
      setAuth(data.user, data.accessToken, data.refreshToken);
      navigate('/events');
    },
    onError: err => {
      setToast(err.response?.data?.message || 'Invalid credentials');
      setTimeout(() => setToast(null), 4000);
    },
  });

  return (
    <div className="auth-page page">
      {/* Left panel */}
      <div className="auth-left">
        <div>
          <div style={{display:'flex',alignItems:'center',gap:10,marginBottom:48}}>
            <div className="brand-icon">🎓</div>
            <span className="brand-name">CampusHub</span>
          </div>
          <div style={{position:'relative',zIndex:1}}>
            <p style={{fontSize:12,letterSpacing:2,textTransform:'uppercase',color:'rgba(255,255,255,0.4)',marginBottom:16}}>Welcome back</p>
            <h2 style={{fontFamily:'var(--font-display)',fontSize:48,fontWeight:700,letterSpacing:-2,lineHeight:1.1,marginBottom:20}}>
              Your campus,<br/>
              <span style={{background:'linear-gradient(135deg,var(--gold),var(--electric-soft))',WebkitBackgroundClip:'text',WebkitTextFillColor:'transparent',backgroundClip:'text'}}>
                reimagined.
              </span>
            </h2>
            <p style={{color:'rgba(255,255,255,0.4)',fontSize:16,lineHeight:1.6,maxWidth:320}}>
              Sign in to register for events, track attendance, and stay connected.
            </p>
          </div>
        </div>
        <div style={{display:'flex',gap:24,position:'relative',zIndex:1}}>
          {['500+','50+','10K+'].map((n, i) => (
            <div key={i}>
              <div style={{fontFamily:'var(--font-display)',fontSize:28,fontWeight:700,color:'var(--gold-light)'}}>{n}</div>
              <div style={{fontSize:12,color:'rgba(255,255,255,0.35)',letterSpacing:0.5,textTransform:'uppercase'}}>
                {['Events','Clubs','Students'][i]}
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Right panel */}
      <div className="auth-right">
        <div className="auth-form-wrap anim-fade-up">
          <h1 className="auth-title">Sign in</h1>
          <p className="auth-subtitle">Enter your college credentials to continue</p>

          {toast && (
            <div style={{padding:'12px 16px',background:'rgba(233,69,96,0.12)',border:'1px solid rgba(233,69,96,0.3)',borderRadius:'var(--radius-md)',fontSize:14,color:'var(--electric)',marginBottom:20}}>
              {toast}
            </div>
          )}

          <form onSubmit={handleSubmit(d => mutation.mutate(d))}>
            <div className="field">
              <label>Email address</label>
              <input type="email" {...register('email')} placeholder="you@college.edu" style={errors.email ? {borderColor:'var(--electric)'} : {}} />
              {errors.email && <div className="field-error">{errors.email.message}</div>}
            </div>

            <div className="field">
              <label>Password</label>
              <div className="input-wrap">
                <input type={showPwd ? 'text' : 'password'} {...register('password')} placeholder="••••••••" style={errors.password ? {borderColor:'var(--electric)'} : {}} />
                <button type="button" className="eye-btn" onClick={() => setShowPwd(!showPwd)}>
                  {showPwd
                    ? <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M17.94 17.94A10.07 10.07 0 0112 20c-7 0-11-8-11-8a18.45 18.45 0 015.06-5.94M9.9 4.24A9.12 9.12 0 0112 4c7 0 11 8 11 8a18.5 18.5 0 01-2.16 3.19m-6.72-1.07a3 3 0 11-4.24-4.24"/><line x1="1" y1="1" x2="23" y2="23"/></svg>
                    : <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
                  }
                </button>
              </div>
              {errors.password && <div className="field-error">{errors.password.message}</div>}
            </div>

            <div style={{textAlign:'right',marginTop:-8,marginBottom:24}}>
              <Link to="/forgot-password" style={{fontSize:13,color:'var(--gold)',textDecoration:'none'}}>Forgot password?</Link>
            </div>

            <button type="submit" disabled={mutation.isPending} className="btn btn-gold btn-lg" style={{width:'100%',justifyContent:'center'}}>
              {mutation.isPending ? 'Signing in...' : 'Sign in →'}
            </button>
          </form>

          <p style={{textAlign:'center',marginTop:24,fontSize:14,color:'rgba(255,255,255,0.35)'}}>
            No account?{' '}
            <Link to="/register" style={{color:'var(--gold)',textDecoration:'none',fontWeight:500}}>Create one</Link>
          </p>
        </div>
      </div>
    </div>
  );
}

// ── Register ──────────────────────────────────────────────────

export function RegisterForm() {
  const [showPwd, setShowPwd] = useState(false);
  const [toast, setToast]     = useState(null);
  const navigate = useNavigate();
  const setAuth  = useAuthStore(s => s.setAuth);

  const { register, handleSubmit, formState: { errors } } = useForm({
    resolver: zodResolver(registerSchema),
  });

  const mutation = useMutation({
    mutationFn: d => authAPI.register(d).then(r => r.data),
    onSuccess: data => {
      setAuth(data.user, data.accessToken, data.refreshToken);
      navigate('/events');
    },
    onError: err => {
      setToast(err.response?.data?.message || 'Registration failed');
      setTimeout(() => setToast(null), 4000);
    },
  });

  return (
    <div className="auth-page page">
      <div className="auth-left">
        <div>
          <div style={{display:'flex',alignItems:'center',gap:10,marginBottom:48}}>
            <div className="brand-icon">🎓</div>
            <span className="brand-name">CampusHub</span>
          </div>
          <div style={{position:'relative',zIndex:1}}>
            <p style={{fontSize:12,letterSpacing:2,textTransform:'uppercase',color:'rgba(255,255,255,0.4)',marginBottom:16}}>Join today</p>
            <h2 style={{fontFamily:'var(--font-display)',fontSize:48,fontWeight:700,letterSpacing:-2,lineHeight:1.1,marginBottom:20}}>
              Start your<br/>
              <span style={{background:'linear-gradient(135deg,var(--teal),var(--gold))',WebkitBackgroundClip:'text',WebkitTextFillColor:'transparent',backgroundClip:'text'}}>
                journey.
              </span>
            </h2>
            <p style={{color:'rgba(255,255,255,0.4)',fontSize:16,lineHeight:1.6,maxWidth:320}}>
              Create your account and unlock access to every event on campus.
            </p>
          </div>
        </div>
        <div style={{display:'flex',flexDirection:'column',gap:16,position:'relative',zIndex:1}}>
          {['Register for any event instantly','Get QR check-in passes','Track your attendance history'].map((f,i) => (
            <div key={i} style={{display:'flex',alignItems:'center',gap:10,fontSize:14,color:'rgba(255,255,255,0.5)'}}>
              <div style={{width:20,height:20,borderRadius:'50%',background:'rgba(201,168,76,0.2)',border:'1px solid rgba(201,168,76,0.4)',display:'flex',alignItems:'center',justifyContent:'center',flexShrink:0}}>
                <svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="var(--gold)" strokeWidth="3"><polyline points="20 6 9 17 4 12"/></svg>
              </div>
              {f}
            </div>
          ))}
        </div>
      </div>

      <div className="auth-right" style={{alignItems:'flex-start',paddingTop:40,overflowY:'auto'}}>
        <div className="auth-form-wrap anim-fade-up">
          <h1 className="auth-title">Create account</h1>
          <p className="auth-subtitle">Fill in your details to get started</p>

          {toast && (
            <div style={{padding:'12px 16px',background:'rgba(233,69,96,0.12)',border:'1px solid rgba(233,69,96,0.3)',borderRadius:'var(--radius-md)',fontSize:14,color:'var(--electric)',marginBottom:20}}>
              {toast}
            </div>
          )}

          <form onSubmit={handleSubmit(d => mutation.mutate(d))}>
            <div className="field-row">
              <div className="field">
                <label>First name</label>
                <input {...register('firstName')} placeholder="Rahul" style={errors.firstName ? {borderColor:'var(--electric)'} : {}} />
                {errors.firstName && <div className="field-error">{errors.firstName.message}</div>}
              </div>
              <div className="field">
                <label>Last name</label>
                <input {...register('lastName')} placeholder="Verma" style={errors.lastName ? {borderColor:'var(--electric)'} : {}} />
                {errors.lastName && <div className="field-error">{errors.lastName.message}</div>}
              </div>
            </div>

            <div className="field">
              <label>College email</label>
              <input type="email" {...register('email')} placeholder="you@college.edu" style={errors.email ? {borderColor:'var(--electric)'} : {}} />
              {errors.email && <div className="field-error">{errors.email.message}</div>}
            </div>

            <div className="field-row">
              <div className="field">
                <label>Roll number</label>
                <input {...register('rollNumber')} placeholder="CS2021001" />
              </div>
              <div className="field">
                <label>Department</label>
                <select {...register('department')} style={errors.department ? {borderColor:'var(--electric)'} : {}}>
                  <option value="">Select...</option>
                  {DEPARTMENTS.map(d => <option key={d} value={d}>{d}</option>)}
                </select>
                {errors.department && <div className="field-error">{errors.department.message}</div>}
              </div>
            </div>

            <div className="field">
              <label>Password</label>
              <div className="input-wrap">
                <input type={showPwd ? 'text' : 'password'} {...register('password')} placeholder="Min 8 characters" style={errors.password ? {borderColor:'var(--electric)'} : {}} />
                <button type="button" className="eye-btn" onClick={() => setShowPwd(!showPwd)}>
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
                </button>
              </div>
              {errors.password && <div className="field-error">{errors.password.message}</div>}
            </div>

            <div className="field">
              <label>Confirm password</label>
              <input type="password" {...register('confirmPassword')} placeholder="••••••••" style={errors.confirmPassword ? {borderColor:'var(--electric)'} : {}} />
              {errors.confirmPassword && <div className="field-error">{errors.confirmPassword.message}</div>}
            </div>

            <button type="submit" disabled={mutation.isPending} className="btn btn-gold btn-lg" style={{width:'100%',justifyContent:'center',marginTop:8}}>
              {mutation.isPending ? 'Creating account...' : 'Create account →'}
            </button>
          </form>

          <p style={{textAlign:'center',marginTop:24,fontSize:14,color:'rgba(255,255,255,0.35)'}}>
            Already have an account?{' '}
            <Link to="/login" style={{color:'var(--gold)',textDecoration:'none',fontWeight:500}}>Sign in</Link>
          </p>
        </div>
      </div>
    </div>
  );
}
