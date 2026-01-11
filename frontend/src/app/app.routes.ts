import { Routes } from '@angular/router';
import { Login } from './pages/login/login';
import { Register } from './register/register';
import { AdminLayout } from './admin/layout/admin-layout/admin-layout';
import { Dashboard } from './admin/dashboard/dashboard';
import { Clients } from './admin/clients/clients';
import { Comptes } from './admin/comptes/comptes';
import { Ajouter } from './admin/ajouter/ajouter';

export const routes: Routes = [
    
    {path: 'login', component: Login },
    {path:'register', component: Register},

    {path: 'admin',
        component: AdminLayout,
        children: [
            { path: 'dashboard', component: Dashboard },
            { path: 'clients', component: Clients,},
            { path: 'comptes', component: Comptes },
            { path: 'clients/ajouter', component: Ajouter },
        ]
    },

    { path: '', redirectTo: '/login', pathMatch: 'full' },
    { path: '**', redirectTo: '/login' },

];
