import { useState } from 'react'
import './App.css'

function App() {
  const [activeMenu, setActiveMenu] = useState('dashboard');

  return (
    <div className="flex h-screen bg-slate-900 text-slate-100">
      {/* Sidebar */}
      <aside className="w-64 bg-slate-800 border-r border-slate-700 flex flex-col">
        <div className="p-6">
          <h1 className="text-2xl font-bold bg-gradient-to-r from-blue-400 to-teal-400 bg-clip-text text-transparent">
            CRM Enterprise
          </h1>
        </div>
        <nav className="flex-1 px-4 space-y-2">
          {['dashboard', 'customers', 'sales', 'analytics', 'settings'].map((item) => (
            <button
              key={item}
              onClick={() => setActiveMenu(item)}
              className={`w-full text-left px-4 py-3 rounded-lg capitalize transition-all duration-200 ${
                activeMenu === item 
                  ? 'bg-blue-600 text-white shadow-lg shadow-blue-500/30' 
                  : 'text-slate-400 hover:bg-slate-700 hover:text-slate-200'
              }`}
            >
              {item}
            </button>
          ))}
        </nav>
        <div className="p-4 border-t border-slate-700">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-full bg-gradient-to-tr from-purple-500 to-pink-500 flex items-center justify-center font-bold">
              TL
            </div>
            <div>
              <p className="font-medium text-sm">Tech Lead</p>
              <p className="text-xs text-slate-400">Admin</p>
            </div>
          </div>
        </div>
      </aside>

      {/* Main Content */}
      <main className="flex-1 flex flex-col overflow-hidden">
        {/* Header */}
        <header className="h-20 border-b border-slate-700 flex items-center justify-between px-8 bg-slate-800/50 backdrop-blur-sm">
          <h2 className="text-xl font-semibold capitalize">{activeMenu} Overview</h2>
          <div className="flex items-center gap-4">
            <input 
              type="text" 
              placeholder="Search anything (Elasticsearch)..." 
              className="bg-slate-900 border border-slate-700 rounded-full px-4 py-2 w-64 focus:outline-none focus:border-blue-500 transition-colors text-sm"
            />
            <button className="bg-slate-700 hover:bg-slate-600 p-2 rounded-full transition-colors">
              🔔
            </button>
          </div>
        </header>

        {/* Dashboard Content */}
        <div className="flex-1 overflow-auto p-8">
          {activeMenu === 'dashboard' && (
            <div className="space-y-6">
              {/* Stats Grid */}
              <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                {[
                  { title: 'Total Revenue', value: '$124,500', growth: '+12%', color: 'from-emerald-500 to-teal-400' },
                  { title: 'Active Customers', value: '8,432', growth: '+5%', color: 'from-blue-500 to-indigo-500' },
                  { title: 'Pending Orders', value: '142', growth: '-2%', color: 'from-orange-500 to-pink-500' }
                ].map((stat, i) => (
                  <div key={i} className="bg-slate-800 rounded-2xl p-6 border border-slate-700 relative overflow-hidden group hover:border-slate-500 transition-colors">
                    <div className={`absolute top-0 right-0 w-32 h-32 bg-gradient-to-br ${stat.color} rounded-full blur-3xl opacity-20 group-hover:opacity-40 transition-opacity`}></div>
                    <h3 className="text-slate-400 text-sm font-medium">{stat.title}</h3>
                    <div className="mt-4 flex items-end gap-3">
                      <span className="text-3xl font-bold">{stat.value}</span>
                      <span className={`text-sm ${stat.growth.startsWith('+') ? 'text-emerald-400' : 'text-rose-400'}`}>
                        {stat.growth}
                      </span>
                    </div>
                  </div>
                ))}
              </div>

              {/* Chart / List Section */}
              <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                <div className="lg:col-span-2 bg-slate-800 rounded-2xl p-6 border border-slate-700">
                  <h3 className="text-lg font-semibold mb-4">Sales Analytics (Real-time Kafka Stream)</h3>
                  <div className="h-64 w-full flex items-end gap-2 pt-8">
                    {[40, 70, 45, 90, 65, 85, 120, 95, 110, 80, 130, 150].map((h, i) => (
                      <div key={i} className="flex-1 bg-blue-500/20 hover:bg-blue-500/40 rounded-t-sm relative group transition-colors" style={{ height: `${(h/150)*100}%` }}>
                        <div className="absolute -top-8 left-1/2 -translate-x-1/2 bg-slate-700 text-xs py-1 px-2 rounded opacity-0 group-hover:opacity-100 transition-opacity">
                          ${h}k
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
                
                <div className="bg-slate-800 rounded-2xl p-6 border border-slate-700">
                  <h3 className="text-lg font-semibold mb-4">Recent Activities</h3>
                  <div className="space-y-4">
                    {[
                      { user: 'Alex H.', action: 'created new order', time: '2m ago' },
                      { user: 'Sarah M.', action: 'updated inventory', time: '15m ago' },
                      { user: 'System', action: 'Elasticsearch sync complete', time: '1h ago', isSystem: true },
                      { user: 'John D.', action: 'registered account', time: '2h ago' }
                    ].map((act, i) => (
                      <div key={i} className="flex items-start gap-3">
                        <div className={`w-8 h-8 rounded-full flex-shrink-0 flex items-center justify-center text-xs font-bold ${act.isSystem ? 'bg-indigo-500/20 text-indigo-400' : 'bg-slate-700'}`}>
                          {act.user.charAt(0)}
                        </div>
                        <div>
                          <p className="text-sm">
                            <span className="font-medium text-slate-200">{act.user}</span> {act.action}
                          </p>
                          <p className="text-xs text-slate-400 mt-0.5">{act.time}</p>
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
              </div>
            </div>
          )}

          {activeMenu !== 'dashboard' && (
            <div className="h-full flex items-center justify-center text-slate-500 flex-col gap-4">
              <div className="text-6xl">🚧</div>
              <p className="text-xl font-medium">{activeMenu} module is under construction</p>
              <p className="text-sm text-slate-600">Connecting to {activeMenu}-service via Spring Cloud Gateway...</p>
            </div>
          )}
        </div>
      </main>
    </div>
  )
}

export default App
