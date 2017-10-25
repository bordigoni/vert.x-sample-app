import {RouterModule, Routes} from '@angular/router';
import {HomeComponent} from './home/home.component';
import {NgModule} from '@angular/core';
import {PageNotFoundComponent} from './page-not-found';

const appRoutes: Routes = [
  {path: 'home', component: HomeComponent},
  {
    path: '',
    redirectTo: '/home',
    pathMatch: 'full'
  },
  {path: '**', component: PageNotFoundComponent}
];

@NgModule({
  imports: [
    RouterModule.forRoot(
      appRoutes,
      {enableTracing: true} // <-- debugging purposes only
    )
  ],
  exports: [
    RouterModule
  ]
})

export class AppRoutingModule {
}
