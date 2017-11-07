import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {PollsourceListComponent} from './pollsource-list/pollsource-list.component';
import {PollsourceEditComponent} from './pollsource-edit/pollsource-edit.component';


const routes: Routes = [
  {path: 'client/:clientId/pollsource', component: PollsourceListComponent},
  {path: 'client/:clientId/pollsource/:id', component: PollsourceEditComponent}
];

@NgModule({
  imports: [
    RouterModule.forChild(routes)
  ],
  exports: [
    RouterModule
  ]
})
export class PollSourceRoutingModule {
}
